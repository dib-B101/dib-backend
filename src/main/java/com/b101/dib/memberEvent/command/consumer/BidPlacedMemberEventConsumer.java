package com.b101.dib.memberEvent.command.consumer;

import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.consumedEvent.command.service.ConsumedEventService;
import com.b101.dib.memberEvent.domain.MemberEvent;
import com.b101.dib.memberEvent.repository.MemberEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

// dib.bid.placed → member_event BID 로그 (입찰 트랜잭션 밖)
@Component
@RequiredArgsConstructor
@Slf4j
public class BidPlacedMemberEventConsumer {
    private final MemberEventRepository memberEventRepository;
    private final ConsumedEventService consumedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.BID_PLACED, groupId = KafkaTopics.GROUP_MEMBER_EVENT)
    @Transactional
    public void onBidPlaced(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        if (!consumedEventService.claim(KafkaTopics.GROUP_MEMBER_EVENT, e.getEventId())) {
            return;
        }
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventId", e.getEventId());
        metadata.put("bidId", e.getLong("bidId"));
        metadata.put("amount", e.getLong("amount"));
        metadata.put("currentPrice", e.getLong("currentPrice"));
        metadata.put("extended", e.getBoolean("extended"));
        LocalDateTime occurredAt = e.getOccurredAt() == null ? null
                : LocalDateTime.ofInstant(Instant.parse(e.getOccurredAt()), ZoneId.systemDefault());
        memberEventRepository.save(MemberEvent.bid(e.getLong("memberId"), e.getLong("auctionId"), e.getLong("productId"),
                e.getLong("categoryId"), objectMapper.writeValueAsString(metadata), occurredAt));
        log.debug("member_event BID 적재 eventId={}", e.getEventId());
    }
}
