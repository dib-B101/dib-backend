package com.b101.dib.websocket.service;

import com.b101.dib.websocket.dto.SocketEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LiveWebSocketService {
    public static final String LIVE_TOPIC = "/topic/live/";     // 구독: /topic/live/{liveBroadcastId}
    public static final String USER_QUEUE = "/queue/live";      // 개인: /user/queue/live

    private final RealtimePublisher realtimePublisher;

    public void broadcast(Long liveBroadcastId, String eventType, Map<String, Object> payload) {
        realtimePublisher.broadcast(LIVE_TOPIC + liveBroadcastId, SocketEnvelope.of(eventType, null, payload));
    }

    public void sendToMember(Long memberId, String eventType, String commandId, Map<String, Object> payload) {
        realtimePublisher.sendToUser(String.valueOf(memberId), USER_QUEUE, SocketEnvelope.of(eventType, commandId, payload));
    }
}
