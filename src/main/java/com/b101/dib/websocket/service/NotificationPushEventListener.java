package com.b101.dib.websocket.service;

import com.b101.dib.common.util.Times;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.domain.NotificationCreatedEvent;
import com.b101.dib.websocket.dto.SocketEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// 알림 행 커밋 → /user/queue/notifications 로 DOMAIN_NOTIFICATION (프론트 DomainNotificationParser 필드)
@Component
@RequiredArgsConstructor
public class NotificationPushEventListener {
    public static final String USER_QUEUE = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onCreated(NotificationCreatedEvent event) {
        Notification n = event.getNotification();
        if (n.getMemberId() == null) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", String.valueOf(n.getNotificationId()));
        payload.put("type", n.getType() == null ? "SYSTEM" : n.getType().name());
        payload.put("resourceType", n.resourceType());
        payload.put("resourceId", String.valueOf(n.resourceId()));
        payload.put("title", n.getTitle());
        payload.put("body", n.getContent());
        payload.put("isRead", false);
        payload.put("occurredAt", Times.iso(n.getCreatedAt()));
        messagingTemplate.convertAndSendToUser(String.valueOf(n.getMemberId()), USER_QUEUE,
                SocketEnvelope.of("DOMAIN_NOTIFICATION", null, payload));
    }
}
