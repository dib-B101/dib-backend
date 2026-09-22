package com.b101.dib.outboxEvent.repository;

import com.b101.dib.outboxEvent.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    // 미발행 행을 오래된 순으로 잠근다. SKIP LOCKED 라 Pod 여러 대가 동시에 돌아도 같은 행을 두 번 발행하지 않는다.
    // attempts 상한을 두는 이유: 영구 실패 행이 제일 오래된 행이라 상한이 없으면 매 폴링마다 먼저 집혀서
    // 배치 슬롯을 차지하고, 그 뒤의 정상 이벤트가 영영 발행되지 않는다
    @Query(value = """
            SELECT * FROM outbox_event
            WHERE published_at IS NULL AND attempts < :maxAttempts
            ORDER BY outbox_event_id
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxEvent> lockUnpublished(@Param("limit") int limit, @Param("maxAttempts") int maxAttempts);

    // 상한을 넘겨 포기한 행. 0 이 아니면 사람이 봐야 한다 — 이벤트가 유실된 것과 같다
    @Query("SELECT COUNT(e) FROM OutboxEvent e WHERE e.publishedAt IS NULL AND e.attempts >= :maxAttempts")
    long countAbandoned(@Param("maxAttempts") int maxAttempts);

    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.publishedAt IS NOT NULL AND e.publishedAt < :before")
    int deletePublishedBefore(@Param("before") LocalDateTime before);
}
