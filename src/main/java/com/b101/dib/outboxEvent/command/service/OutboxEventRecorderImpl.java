package com.b101.dib.outboxEvent.command.service;

import com.b101.dib.outboxEvent.domain.OutboxEvent;
import com.b101.dib.outboxEvent.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OutboxEventRecorderImpl implements OutboxEventRecorder {
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)   // 반드시 도메인 트랜잭션 안에서 — 밖에서 부르면 예외
    public void record(String aggregateType, Long aggregateId, String eventType, String topic, Map<String, Object> payload) {
        outboxEventRepository.save(OutboxEvent.of(aggregateType, aggregateId, eventType, topic, objectMapper.writeValueAsString(payload)));
    }
}
