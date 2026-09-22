package com.b101.dib.outboxEvent.command.service;

public interface OutboxPublishTxService {
    // 미발행 행을 잠그고 Kafka 로 보낸 뒤 published_at 을 채운다. 발행한 개수 반환
    int publishBatch(int limit);

    // 재시도 상한을 넘겨 포기한 행 수. 0 이 아니면 그만큼 이벤트가 유실된 것과 같다
    long countAbandoned();

    // 발행 완료 후 보관 기간이 지난 행 삭제. 삭제한 개수 반환
    int purgePublished();
}
