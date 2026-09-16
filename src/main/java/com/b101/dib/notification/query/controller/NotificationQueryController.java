package com.b101.dib.notification.query.controller;

import com.b101.dib.notification.query.service.NotificationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationQueryController {
    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(@RequestHeader("X-Member-Id") Long memberId,
                                                        @RequestParam(name = "unreadOnly", required = false) Boolean unreadOnly,
                                                        @RequestParam(name = "cursor", required = false) String cursor,
                                                        @RequestParam(name = "size", defaultValue = "20") int size) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "알림 목록 조회 성공");
        map.put("data", notificationQueryService.findMine(memberId, unreadOnly, cursor, size));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount(@RequestHeader("X-Member-Id") Long memberId) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("unreadCount", notificationQueryService.countUnread(memberId));
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "안 읽은 알림 수 조회 성공");
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
