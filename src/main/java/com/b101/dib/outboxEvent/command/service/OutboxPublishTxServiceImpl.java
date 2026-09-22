package com.b101.dib.outboxEvent.command.service;

import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.util.Times;
import com.b101.dib.outboxEvent.domain.OutboxEvent;
import com.b101.dib.outboxEvent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublishTxServiceImpl implements OutboxPublishTxService {
    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${dib.outbox.max-attempts:10}")
    private int maxAttempts;

    @Value("${dib.outbox.retention-days:7}")
    private int retentionDays;

    @Override
    @Transactional
    public int publishBatch(int limit) {
        List<OutboxEvent> events = outboxEventRepository.lockUnpublished(limit, maxAttempts);
        int published = 0;
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), String.valueOf(event.getAggregateId()), toEnvelope(event))
                        .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);   // 브로커 ack 를 기다린 뒤에만 발행 완료로 표시
                event.markPublished();
                published++;
            } catch (Exception e) {
                event.markFailed(e.getMessage());   // 행은 남으므로 다음 폴링에서 재시도 — 유실 없음
                if (event.getAttempts() >= maxAttempts) {
                    // 여기 걸리면 더는 재시도하지 않는다. 사람이 원인을 고치고 attempts 를 0 으로 되돌려야 한다
                    log.error("outbox 발행 포기 (재시도 {}회 초과) eventId={} topic={} lastError={}",
                            maxAttempts, event.getEventId(), event.getTopic(), event.getLastError());
                } else {
                    log.warn("outbox 발행 실패 eventId={} topic={} attempts={}",
                            event.getEventId(), event.getTopic(), event.getAttempts());
                }
            }
        }
        return published;
    }

    @Override
    @Transactional(readOnly = true)
    public long countAbandoned() {
        return outboxEventRepository.countAbandoned(maxAttempts);
    }

    @Override
    @Transactional
    public int purgePublished() {
        return outboxEventRepository.deletePublishedBefore(LocalDateTime.now().minusDays(retentionDays));
    }

    private EventEnvelope toEnvelope(OutboxEvent event) {
        EventEnvelope envelope = new EventEnvelope();
        envelope.setEventType(event.getEventType());
        envelope.setEventId(event.getEventId());
        envelope.setOccurredAt(Times.iso(event.getOccurredAt()));
        envelope.setAggregateType(event.getAggregateType());
        envelope.setAggregateId(event.getAggregateId());
        envelope.setPayload(objectMapper.readValue(event.getPayload(), new TypeReference<Map<String, Object>>() {}));
        return envelope;
    }
}
