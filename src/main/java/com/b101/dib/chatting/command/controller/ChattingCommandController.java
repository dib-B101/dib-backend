package com.b101.dib.chatting.command.controller;

import com.b101.dib.chatting.command.dto.SendChattingRequest;
import com.b101.dib.chatting.command.service.ChattingCommandService;
import com.b101.dib.chatting.domain.Chatting;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.websocket.service.OrderChatWebSocketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ChattingCommandController {
    private final ChattingCommandService chattingCommandService;
    private final MemberRepository memberRepository;
    private final OrderChatWebSocketService orderChatWebSocketService;

    // 소켓(/app/orders/{id}/messages)과 같은 서비스. REST 로 보낸 것도 구독자에게 CHAT_MESSAGE_CREATED 로 흘린다
    @PostMapping("/{orderId}/messages")
    public ResponseEntity<Map<String, Object>> send(@RequestHeader("X-Member-Id") Long memberId,
                                                    @PathVariable("orderId") Long orderId,
                                                    @Valid @RequestBody SendChattingRequest request) {
        Chatting chatting = chattingCommandService.send(memberId, orderId, request.getContent());
        Member sender = memberRepository.findById(memberId).orElse(null);
        orderChatWebSocketService.broadcastCreated(chatting, sender == null ? null : sender.getNickname());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "메시지 전송 성공");
        map.put("data", chatting);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
