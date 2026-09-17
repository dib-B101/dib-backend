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

// dib.auction.closed → 낙찰자 AUCTION_WON 알림 (종료 트랜잭션 밖)
@Component
@RequiredArgsConstructor
public class AuctionClosedNotificationConsumer {
    private final NotificationRepository notificationRepository;
    private final ConsumedEventService consumedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.AUCTION_CLOSED, groupId = KafkaTopics.GROUP_NOTIFICATION)
    @Transactional
    public void onAuctionClosed(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        if (!consumedEventService.claim(KafkaTopics.GROUP_NOTIFICATION, e.getEventId())) {
            return;
        }
        if (!"SOLD".equals(e.getString("result"))) {
            return;
        }
        notificationRepository.save(Notification.won(e.getLong("auctionId"), e.getLong("winnerId"), e.getLong("topBidId"), e.getLong("finalPrice")));
    }
}
