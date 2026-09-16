package com.b101.dib.outboxEvent.command.service;

public interface OutboxPublishTxService {
    // 미발행 행을 잠그고 Kafka 로 보낸 뒤 published_at 을 채운다. 발행한 개수 반환
    int publishBatch(int limit);
}
