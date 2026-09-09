package com.b101.dib.order.query.controller;

import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.dto.OrderQueryDto;
import com.b101.dib.order.query.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {
    private final OrderQueryService orderQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(@RequestHeader("X-Member-Id") Long memberId,
                                                        @RequestParam(name = "role", required = false) OrderRole role,
                                                        @RequestParam(name = "status", required = false) OrderStatus status) {
        List<OrderQueryDto> dtoList = orderQueryService.findMine(memberId, role, status);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "내 주문 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> findDetail(@RequestHeader("X-Member-Id") Long memberId,
                                                          @PathVariable("orderId") Long orderId) {
        OrderDetailDto dto = orderQueryService.findDetail(memberId, orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "주문 상세 조회 성공");
        map.put("data", dto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
