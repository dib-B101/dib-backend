package com.b101.dib.notification.command.consumer;

import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.consumedEvent.command.service.ConsumedEventService;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

// dib.bid.placed → 밀린 이전 최고입찰자에게 OUTBID 알림. 저장되면 NotificationPushListener 가 소켓으로 푸시
@Component
@RequiredArgsConstructor
public class BidPlacedNotificationConsumer {
    private final NotificationRepository notificationRepository;
    private final ConsumedEventService consumedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.BID_PLACED, groupId = KafkaTopics.GROUP_NOTIFICATION)
    @Transactional
    public void onBidPlaced(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        if (!consumedEventService.claim(KafkaTopics.GROUP_NOTIFICATION, e.getEventId())) {
            return;
        }
        Long previousTopBidderId = e.getLong("previousTopBidderId");
        if (previousTopBidderId == null) {
            return;   // 첫 입찰 — 밀린 사람 없음
        }
        notificationRepository.save(Notification.outbid(e.getLong("auctionId"), previousTopBidderId, e.getLong("bidId"), e.getLong("amount")));
    }
}
