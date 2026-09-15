package com.b101.dib.chatting.command.controller;

import com.b101.dib.chatting.command.dto.SendChattingRequest;
import com.b101.dib.chatting.command.service.ChattingCommandService;
import com.b101.dib.chatting.domain.Chatting;
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

    // 웹소켓 핸들러가 붙기 전까지의 REST 전송. 소켓이 생기면 같은 서비스를 @MessageMapping 에서 호출한다
    @PostMapping("/{orderId}/messages")
    public ResponseEntity<Map<String, Object>> send(@RequestHeader("X-Member-Id") Long memberId,
                                                    @PathVariable("orderId") Long orderId,
                                                    @Valid @RequestBody SendChattingRequest request) {
        Chatting chatting = chattingCommandService.send(memberId, orderId, request.getContent());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "메시지 전송 성공");
        map.put("data", chatting);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
