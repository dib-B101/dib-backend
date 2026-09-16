package com.b101.dib.outboxEvent.repository;

import com.b101.dib.outboxEvent.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    // 미발행 행을 오래된 순으로 잠근다. SKIP LOCKED 라 Pod 여러 대가 동시에 돌아도 같은 행을 두 번 발행하지 않는다
    @Query(value = "SELECT * FROM outbox_event WHERE published_at IS NULL ORDER BY outbox_event_id LIMIT :limit FOR UPDATE SKIP LOCKED",
            nativeQuery = true)
    List<OutboxEvent> lockUnpublished(@Param("limit") int limit);
}
