package com.b101.dib.outboxEvent.command.service;

import java.util.Map;

// 도메인 서비스가 트랜잭션 안에서 호출한다. 커밋되면 OutboxPublisher 가 Kafka 로 보낸다
public interface OutboxEventRecorder {
    void record(String aggregateType, Long aggregateId, String eventType, String topic, Map<String, Object> payload);
}
