package com.b101.dib.common.messaging;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// DLT 전용 프로듀서. 메시지 값이 String(StringDeserializer) 이라 DLT 에도 String 그대로 넣는다 —
// 기본 KafkaTemplate 은 JSON 직렬화라 문자열이 한 번 더 감싸져서 사람이 읽기 나빠진다.
//
// 템플릿과 팩토리를 @Bean 으로 노출하지 않는 게 중요하다. Boot 의 KafkaAutoConfiguration 이
// KafkaTemplate/ProducerFactory 를 @ConditionalOnMissingBean 으로 만들기 때문에,
// 여기서 빈으로 올리면 Outbox 발행이 쓰는 기본 KafkaTemplate 이 통째로 사라진다.
@Component
public class DeadLetterProducer implements DisposableBean {

    private final DefaultKafkaProducerFactory<Object, Object> producerFactory;
    private final KafkaTemplate<Object, Object> template;

    public DeadLetterProducer(@Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.producerFactory = new DefaultKafkaProducerFactory<>(properties);
        this.template = new KafkaTemplate<>(producerFactory);
    }

    public KafkaTemplate<Object, Object> template() {
        return template;
    }

    @Override
    public void destroy() {
        producerFactory.destroy();
    }
}
