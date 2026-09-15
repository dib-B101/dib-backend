package com.b101.dib.notification.command.controller;

import com.b101.dib.notification.command.service.NotificationCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationCommandController {
    private final NotificationCommandService notificationCommandService;

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Map<String, Object>> read(@RequestHeader("X-Member-Id") Long memberId,
                                                    @PathVariable("notificationId") Long notificationId) {
        notificationCommandService.read(memberId, notificationId);
        HashMap<String, Object> data = new HashMap<>();
        data.put("notificationId", notificationId);
        data.put("isRead", true);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "알림 읽음 처리 성공");
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Object>> readAll(@RequestHeader("X-Member-Id") Long memberId) {
        int updated = notificationCommandService.readAll(memberId);
        HashMap<String, Object> data = new HashMap<>();
        data.put("updatedCount", updated);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "알림 전체 읽음 처리 성공");
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
