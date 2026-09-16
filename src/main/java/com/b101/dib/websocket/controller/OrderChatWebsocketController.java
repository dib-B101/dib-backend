package com.b101.dib.websocket.controller;

import com.b101.dib.chatting.command.service.ChattingCommandService;
import com.b101.dib.chatting.domain.Chatting;
import com.b101.dib.chatting.query.dto.ChattingQueryDto;
import com.b101.dib.chatting.repository.ChattingMapper;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.Times;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.websocket.dto.SendChatCommand;
import com.b101.dib.websocket.dto.SocketEnvelope;
import com.b101.dib.websocket.service.OrderChatWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// STOMP. 구독 /topic/orders/{orderId} (CHAT_MESSAGE_CREATED), /user/queue/orders (CHAT_MESSAGE_ACCEPTED / ERROR)
//        전송 /app/orders/{orderId}/messages {commandId, content}
//        스냅샷 /app/orders/{orderId}/snapshot 구독하면 ORDER_SNAPSHOT 1회 (status, chattingReadOnly, lastChattingId)
// 규칙은 REST 와 같은 Chatting.send() 에서 (참여자 아님 403, 종료 409, 빈/500자 400)
@Controller
@RequiredArgsConstructor
public class OrderChatWebsocketController {
    private final ChattingCommandService chattingCommandService;
    private final ChattingMapper chattingMapper;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final OrderChatWebSocketService orderChatWebSocketService;

    @MessageMapping("/orders/{orderId}/messages")
    public void send(@DestinationVariable("orderId") Long orderId, SendChatCommand command, Principal principal) {
        Long memberId = memberId(principal);
        if (memberId == null) {
            return;
        }
        try {
            Chatting chatting = chattingCommandService.send(memberId, orderId, command.getContent());
            Map<String, Object> accepted = new HashMap<>();
            accepted.put("commandId", command.getCommandId());
            accepted.put("chattingId", String.valueOf(chatting.getChattingId()));
            accepted.put("orderId", String.valueOf(orderId));
            accepted.put("time", Times.iso(chatting.getTime()));
            orderChatWebSocketService.sendToMember(memberId, "CHAT_MESSAGE_ACCEPTED", command.getCommandId(), accepted);
            orderChatWebSocketService.broadcastCreated(chatting, nickname(memberId));
        } catch (BusinessException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("commandId", command.getCommandId());
            error.put("orderId", String.valueOf(orderId));
            error.put("code", e.getErrorCode().name());
            error.put("message", e.getErrorCode().getMessage());
            error.put("retryable", false);
            orderChatWebSocketService.sendToMember(memberId, "ERROR", command.getCommandId(), error);
        }
    }

    @SubscribeMapping("/orders/{orderId}/snapshot")
    public SocketEnvelope snapshot(@DestinationVariable("orderId") Long orderId, Principal principal) {
        Long memberId = memberId(principal);
        Order order = orderRepository.findById(orderId).orElse(null);
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", String.valueOf(orderId));
        if (order == null || memberId == null || !order.isParticipant(memberId)) {
            payload.put("code", ErrorCode.FORBIDDEN.name());
            return SocketEnvelope.of("ERROR", null, payload);
        }
        payload.put("status", order.getStatus().name());
        payload.put("chattingReadOnly", order.isClosed());
        List<ChattingQueryDto> last = chattingMapper.findByOrderId(orderId, null, 1);
        payload.put("lastChattingId", last.isEmpty() ? null : String.valueOf(last.get(0).getChattingId()));
        payload.put("serverTime", Times.now());
        return SocketEnvelope.of("ORDER_SNAPSHOT", null, payload);
    }

    private String nickname(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return member == null ? null : member.getNickname();
    }

    private static Long memberId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
