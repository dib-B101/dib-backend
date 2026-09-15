package com.b101.dib.order.query.controller;

import com.b101.dib.order.query.service.ShipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ShipmentQueryController {
    private final ShipmentQueryService shipmentQueryService;

    @GetMapping("/{orderId}/shipping-address")
    public ResponseEntity<Map<String, Object>> findAddress(@RequestHeader("X-Member-Id") Long memberId,
                                                           @PathVariable("orderId") Long orderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "배송지 조회 성공");
        map.put("data", shipmentQueryService.findAddress(memberId, orderId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/{orderId}/shipment")
    public ResponseEntity<Map<String, Object>> findShipment(@RequestHeader("X-Member-Id") Long memberId,
                                                            @PathVariable("orderId") Long orderId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "배송 상태 조회 성공");
        map.put("data", shipmentQueryService.findShipment(memberId, orderId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
