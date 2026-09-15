package com.b101.dib.order.command.controller;

import com.b101.dib.order.command.dto.ShipmentRequest;
import com.b101.dib.order.command.dto.UpdateAddressRequest;
import com.b101.dib.order.command.service.ShipmentCommandService;
import com.b101.dib.order.domain.Order;
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
public class ShipmentCommandController {
    private final ShipmentCommandService shipmentCommandService;

    @PatchMapping("/{orderId}/address")
    public ResponseEntity<Map<String, Object>> updateAddress(@RequestHeader("X-Member-Id") Long memberId,
                                                             @PathVariable("orderId") Long orderId,
                                                             @RequestBody @Valid UpdateAddressRequest request) {
        Order order = shipmentCommandService.updateAddress(memberId, orderId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "배송지 등록 성공");
        map.put("order", order);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @PostMapping("/{orderId}/shipment")
    public ResponseEntity<Map<String, Object>> ship(@RequestHeader("X-Member-Id") Long memberId,
                                                    @PathVariable("orderId") Long orderId,
                                                    @RequestBody @Valid ShipmentRequest request) {
        Order order = shipmentCommandService.ship(memberId, orderId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "송장 등록 성공");
        map.put("order", order);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
