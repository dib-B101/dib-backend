package com.b101.dib.websocket.service;

import com.b101.dib.chatting.domain.Chatting;
import com.b101.dib.common.util.Times;
import com.b101.dib.websocket.dto.SocketEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderChatWebSocketService {
    public static final String ORDER_TOPIC = "/topic/orders/";   // 구독: /topic/orders/{orderId}
    public static final String USER_QUEUE = "/queue/orders";     // 개인: /user/queue/orders

    private final SimpMessagingTemplate messagingTemplate;

    // 새 메시지 → 주문 참여자 모두 (REST 로 보낸 것도 여기로 흘려서 소켓 클라이언트가 같이 본다)
    public void broadcastCreated(Chatting chatting, String memberNickname) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("chattingId", String.valueOf(chatting.getChattingId()));
        payload.put("orderId", String.valueOf(chatting.getOrderId()));
        payload.put("memberId", String.valueOf(chatting.getMemberId()));
        payload.put("memberNickname", memberNickname);
        payload.put("content", chatting.getContent());
        payload.put("time", Times.iso(chatting.getTime()));
        messagingTemplate.convertAndSend(ORDER_TOPIC + chatting.getOrderId(), SocketEnvelope.of("CHAT_MESSAGE_CREATED", null, payload));
    }

    public void sendToMember(Long memberId, String eventType, String commandId, Map<String, Object> payload) {
        messagingTemplate.convertAndSendToUser(String.valueOf(memberId), USER_QUEUE, SocketEnvelope.of(eventType, commandId, payload));
    }
}
