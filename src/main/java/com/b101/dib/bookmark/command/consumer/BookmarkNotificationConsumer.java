package com.b101.dib.bookmark.command.consumer;

import com.b101.dib.bookmark.repository.BookmarkRepository;
import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.consumedEvent.command.service.ConsumedEventService;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// 찜한 상품에 일이 생기면 찜한 사람에게 알린다.
// 찜을 누르는 이유가 "시작하면 알려줘" 인데 그동안 이 경로가 통째로 없었다 —
// NotificationType 에 BOOKMARK_STARTED / LIVE_STARTED 가 정의만 돼 있고 만드는 코드가 없었다.
//
// 한 상품을 수백 명이 찜했을 수 있어서 입찰·경매 트랜잭션 안에서 하면 안 된다. 그래서 Consumer 다.
@Component
@RequiredArgsConstructor
@Slf4j
public class BookmarkNotificationConsumer {

    private final BookmarkRepository bookmarkRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final ConsumedEventService consumedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.AUCTION_STARTED, groupId = KafkaTopics.GROUP_BOOKMARK)
    @Transactional
    public void onAuctionStarted(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        if (!consumedEventService.claim(KafkaTopics.GROUP_BOOKMARK, e.getEventId())) {
            return;
        }
        Long auctionId = e.getLong("auctionId");
        Long productId = e.getLong("productId");
        Long sellerId = e.getLong("sellerId");
        if (productId == null) {
            return;
        }
        String title = title(e, productId);

        List<Notification> notifications = new ArrayList<>();
        for (Long memberId : bookmarkRepository.findMemberIdsByProductId(productId)) {
            // 판매자가 자기 상품을 찜해 뒀을 수 있다. 자기 경매 시작 알림은 의미가 없다
            if (memberId == null || memberId.equals(sellerId)) {
                continue;
            }
            notifications.add(Notification.bookmarkStarted(auctionId, productId, memberId, title));
        }
        saveAll(notifications, "AUCTION_STARTED", auctionId);
    }

    @KafkaListener(topics = KafkaTopics.LIVE_STARTED, groupId = KafkaTopics.GROUP_BOOKMARK)
    @Transactional
    public void onLiveStarted(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        if (!consumedEventService.claim(KafkaTopics.GROUP_BOOKMARK, e.getEventId())) {
            return;
        }
        Long liveBroadcastId = e.getAggregateId();
        List<Long> productIds = longList(e.getPayload().get("productIds"));
        if (productIds.isEmpty()) {
            return;
        }

        Map<Long, String> titles = new HashMap<>();
        productRepository.findAllById(productIds)
                .forEach(p -> titles.put(p.getProductId(), titleOf(p)));

        // (회원, 방송) 당 한 번만. 한 사람이 그 방송의 물건을 셋 찜했다고 알림이 셋 오면 안 된다
        Map<Long, Long> firstProductByMember = new HashMap<>();
        for (Object[] row : bookmarkRepository.findProductAndMemberIdsIn(productIds)) {
            Long productId = (Long) row[0];
            Long memberId = (Long) row[1];
            if (memberId == null || productId == null) {
                continue;
            }
            firstProductByMember.putIfAbsent(memberId, productId);
        }

        List<Notification> notifications = new ArrayList<>();
        firstProductByMember.forEach((memberId, productId) -> notifications.add(
                Notification.liveStarted(liveBroadcastId, productId, memberId,
                        titles.getOrDefault(productId, "찜한 상품"))));
        saveAll(notifications, "LIVE_STARTED", liveBroadcastId);
    }

    private void saveAll(List<Notification> notifications, String eventType, Long aggregateId) {
        if (notifications.isEmpty()) {
            return;
        }
        notificationRepository.saveAll(notifications);
        log.info("{} 찜 알림 {}건 aggregateId={}", eventType, notifications.size(), aggregateId);
    }

    private String title(EventEnvelope e, Long productId) {
        Object fromPayload = e.getPayload().get("productTitle");
        if (fromPayload instanceof String s && !s.isBlank()) {
            return s;
        }
        return productRepository.findById(productId).map(this::titleOf).orElse("찜한 상품");
    }

    private String titleOf(Product product) {
        return product.getTitle() == null || product.getTitle().isBlank() ? "찜한 상품" : product.getTitle();
    }

    @SuppressWarnings("unchecked")
    private static List<Long> longList(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<Long> ids = new ArrayList<>();
        for (Object item : (List<Object>) list) {
            if (item instanceof Number n) {
                ids.add(n.longValue());
            } else if (item != null) {
                try {
                    ids.add(Long.valueOf(item.toString()));
                } catch (NumberFormatException ignored) {
                    // 스키마가 바뀌어 이상한 값이 와도 알림 하나 빠지는 것으로 끝낸다
                }
            }
        }
        ids.removeIf(Objects::isNull);
        return ids;
    }
}
