package com.b101.dib.common.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

// 두 가지를 확인한다.
// 1) DefaultErrorHandler 를 빈으로 두면 Boot 가 리스너 컨테이너에 자동으로 꽂아 준다는 전제.
//    안 꽂히면 기본값(10회 즉시 재시도 후 스킵)이 그대로라 DLT 설정 전체가 무의미해진다.
// 2) DLT 전용 프로듀서를 만들면서 기본 KafkaTemplate 이 사라지지 않았는지.
//    Boot 의 KafkaAutoConfiguration 은 KafkaTemplate 을 @ConditionalOnMissingBean 으로 만들기 때문에
//    DLT 템플릿을 @Bean 으로 올리면 Outbox 발행이 쓰는 기본 템플릿이 통째로 없어진다.
@SpringBootTest
class KafkaErrorHandlingConfigTest {

    @Autowired DefaultErrorHandler kafkaErrorHandler;
    @Autowired ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory;
    @Autowired KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void wiresErrorHandlerIntoListenerContainers() {
        MessageListenerContainer container = kafkaListenerContainerFactory.createContainer("dib.bid.placed");

        // getCommonErrorHandler() 가 protected 라 필드로 본다
        assertThat(ReflectionTestUtils.getField(container, "commonErrorHandler")).isSameAs(kafkaErrorHandler);
    }

    @Test
    void keepsDefaultKafkaTemplateForOutboxPublishing() {
        assertThat(kafkaTemplate).isNotNull();
    }
}
