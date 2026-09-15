package com.b101.dib.chatting.query.controller;

import com.b101.dib.chatting.query.dto.ChattingPageDto;
import com.b101.dib.chatting.query.service.ChattingQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ChattingQueryController {
    private final ChattingQueryService chattingQueryService;

    @GetMapping("/{orderId}/messages")
    public ResponseEntity<Map<String, Object>> findMessages(@RequestHeader("X-Member-Id") Long memberId,
                                                            @PathVariable("orderId") Long orderId,
                                                            @RequestParam(name = "beforeChattingId", required = false) Long beforeChattingId,
                                                            @RequestParam(name = "size", defaultValue = "30") int size) {
        ChattingPageDto page = chattingQueryService.findMessages(memberId, orderId, beforeChattingId, size);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "채팅 내역 조회 성공");
        map.put("items", page.getItems());
        map.put("hasMore", page.isHasMore());
        map.put("chattingReadOnly", page.isChattingReadOnly());
        return ResponseEntity.ok(map);
    }
}
