package com.b101.dib.websocket.service;

import com.b101.dib.websocket.dto.SocketEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuctionWebSocketService {
    public static final String AUCTION_TOPIC = "/topic/auctions/";     // 구독: /topic/auctions/{auctionId}
    public static final String USER_QUEUE = "/queue/auction";          // 개인: /user/queue/auction

    private final RealtimePublisher realtimePublisher;

    public void broadcast(Long auctionId, String eventType, Map<String, Object> payload) {
        realtimePublisher.broadcast(AUCTION_TOPIC + auctionId, SocketEnvelope.of(eventType, null, payload));
    }

    public void sendToMember(Long memberId, String eventType, String commandId, Map<String, Object> payload) {
        realtimePublisher.sendToUser(String.valueOf(memberId), USER_QUEUE, SocketEnvelope.of(eventType, commandId, payload));
    }
}
