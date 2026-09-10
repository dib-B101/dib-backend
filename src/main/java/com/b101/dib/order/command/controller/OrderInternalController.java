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
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class OrderInternalController {
    private final OrderCommandService orderCommandService;

    @PostMapping("/auctions/{auctionId}/orders")
    public ResponseEntity<Map<String, Object>> create(@PathVariable("auctionId") Long auctionId) {
        Order order = orderCommandService.create(auctionId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "주문 생성 성공");
        map.put("orderId", order.getOrderId());
        map.put("auctionId", order.getAuctionId());
        map.put("sellerId", order.getSellerId());
        map.put("buyerId", order.getBuyerId());
        map.put("finalPrice", order.getFinalPrice());
        map.put("status", order.getStatus());
        map.put("paymentDue", order.getPaymentDue());
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
