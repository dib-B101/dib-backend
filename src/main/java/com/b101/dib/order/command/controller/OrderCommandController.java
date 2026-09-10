package com.b101.dib.order.command.controller;

import com.b101.dib.order.command.service.OrderCommandService;
import com.b101.dib.order.domain.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCommandController {
    private final OrderCommandService orderCommandService;

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@RequestHeader("X-Member-Id") Long memberId,
                                                       @PathVariable("orderId") Long orderId) {
        Order order = orderCommandService.confirm(memberId, orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "구매 확정 성공");
        map.put("orderId", order.getOrderId());
        map.put("status", order.getStatus());
        map.put("updatedAt", order.getUpdatedAt());
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
