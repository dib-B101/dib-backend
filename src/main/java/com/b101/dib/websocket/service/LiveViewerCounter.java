package com.b101.dib.websocket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

// /topic/live/{id} 구독 수 = 시청자 수.
// 이 Pod 에 붙은 세션만 세므로 다중 Pod 에서는 정확하지 않다 (Redis 카운터 이관은 이번 범위 밖)
@Component
@RequiredArgsConstructor
public class LiveViewerCounter {
    private final LiveWebSocketService liveWebSocketService;

    private final Map<String, Long> subscriptions = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> viewers = new ConcurrentHashMap<>();

    @EventListener
    public void onSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long liveBroadcastId = liveBroadcastId(accessor.getDestination());
        if (liveBroadcastId == null) {
            return;
        }
        String key = key(accessor.getSessionId(), accessor.getSubscriptionId());
        if (key == null || subscriptions.putIfAbsent(key, liveBroadcastId) != null) {
            return;
        }
        broadcast(liveBroadcastId, viewers.computeIfAbsent(liveBroadcastId, id -> new AtomicInteger()).incrementAndGet());
    }

    @EventListener
    public void onUnsubscribe(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        release(key(accessor.getSessionId(), accessor.getSubscriptionId()));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (sessionId == null) {
            return;
        }
        String prefix = sessionId + "#";
        for (String key : new ArrayList<>(subscriptions.keySet())) {
            if (key.startsWith(prefix)) {
                release(key);
            }
        }
    }

    public int count(Long liveBroadcastId) {
        AtomicInteger counter = viewers.get(liveBroadcastId);
        return counter == null ? 0 : Math.max(0, counter.get());
    }

    private void release(String key) {
        if (key == null) {
            return;
        }
        Long liveBroadcastId = subscriptions.remove(key);
        if (liveBroadcastId == null) {
            return;
        }
        AtomicInteger counter = viewers.get(liveBroadcastId);
        if (counter == null) {
            return;
        }
        int remaining = counter.decrementAndGet();
        if (remaining < 0) {
            counter.set(0);
            remaining = 0;
        }
        broadcast(liveBroadcastId, remaining);
    }

    private void broadcast(Long liveBroadcastId, int viewerCount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("liveBroadcastId", String.valueOf(liveBroadcastId));
        payload.put("viewerCount", viewerCount);
        liveWebSocketService.broadcast(liveBroadcastId, "LIVE_VIEWER_COUNT_UPDATED", payload);
    }

    private static String key(String sessionId, String subscriptionId) {
        return sessionId == null || subscriptionId == null ? null : sessionId + "#" + subscriptionId;
    }

    private static Long liveBroadcastId(String destination) {
        if (destination == null || !destination.startsWith(LiveWebSocketService.LIVE_TOPIC)) {
            return null;
        }
        String raw = destination.substring(LiveWebSocketService.LIVE_TOPIC.length());
        if (raw.isEmpty() || raw.indexOf('/') >= 0) {
            return null;
        }
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
