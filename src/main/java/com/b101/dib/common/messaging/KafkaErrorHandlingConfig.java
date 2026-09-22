package com.b101.dib.common.messaging;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import tools.jackson.core.JacksonException;

// Consumer 가 던진 예외를 어떻게 처리할지. 이게 없으면 Spring 기본값(10회 즉시 재시도 후 로그만 남기고 스킵)이
// 적용돼서, 알림·정산·이상탐지 이벤트가 버려져도 무엇이 왜 버려졌는지 남는 곳이 없다.
//
// 흐름: 실패 → 1s, 2s, 4s 로 3회 재시도 → 그래도 실패하면 <토픽>.DLT 로 옮기고 오프셋 커밋.
// 실패한 메시지를 DLT 로 빼야 뒤에 쌓인 정상 메시지가 안 막힌다(파티션 블로킹 방지).
// 소비는 멱등하므로(ConsumedEventService.claim) 재시도가 중복 처리를 만들지 않는다.
@Configuration
@Slf4j
public class KafkaErrorHandlingConfig {

    private static final int MAX_RETRIES = 3;
    public static final String DLT_SUFFIX = ".DLT";

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterProducer deadLetterProducer,
            MeterRegistry meterRegistry
    ) {
        DeadLetterPublishingRecoverer publisher = new DeadLetterPublishingRecoverer(
                deadLetterProducer.template(),
                // 파티션을 -1 로 둬서 브로커가 고르게 한다. DLT 파티션 수가 원본보다 적을 수 있다
                (record, exception) -> new TopicPartition(record.topic() + DLT_SUFFIX, -1));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Consumer 처리 실패 — DLT 로 보냄 topic={} partition={} offset={} key={} value={}",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value(), exception);
                    meterRegistry.counter("dib.kafka.dlt",
                            "topic", String.valueOf(record.topic()),
                            "exception", exception.getClass().getSimpleName()).increment();
                    publisher.accept(record, exception);
                },
                backOff());

        // 메시지 자체가 깨졌으면 몇 번을 다시 읽어도 같다 — 재시도 없이 바로 DLT 로
        errorHandler.addNotRetryableExceptions(JacksonException.class);
        return errorHandler;
    }

    private static ExponentialBackOff backOff() {
        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxAttempts(MAX_RETRIES);
        backOff.setMaxInterval(10_000L);
        return backOff;
    }
}
