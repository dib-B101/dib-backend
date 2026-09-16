package com.b101.dib.outboxEvent.command.service;

import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.util.Times;
import com.b101.dib.outboxEvent.domain.OutboxEvent;
import com.b101.dib.outboxEvent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

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

    @Override
    @Transactional
    public int publishBatch(int limit) {
        List<OutboxEvent> events = outboxEventRepository.lockUnpublished(limit);
        int published = 0;
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.getTopic(), String.valueOf(event.getAggregateId()), toEnvelope(event))
                        .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);   // 브로커 ack 를 기다린 뒤에만 발행 완료로 표시
                event.markPublished();
                published++;
            } catch (Exception e) {
                event.markFailed(e.getMessage());   // 행은 남으므로 다음 폴링에서 재시도 — 유실 없음
                log.warn("outbox 발행 실패 eventId={} topic={} attempts={}", event.getEventId(), event.getTopic(), event.getAttempts());
            }
        }
        return published;
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
