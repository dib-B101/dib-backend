package com.b101.dib.order.command.controller;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.order.command.service.OrderCommandService;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.service.OrderInternalQueryService;
import com.b101.dib.payment.command.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
@Slf4j
public class OrderInternalController {
    private final OrderCommandService orderCommandService;
    private final OrderInternalQueryService orderInternalQueryService;
    private final PaymentCommandService paymentCommandService;

    @PostMapping("/auctions/{auctionId}/orders")
    public ResponseEntity<Map<String, Object>> create(@PathVariable("auctionId") Long auctionId) {
        Order order = orderCommandService.create(auctionId);
        String paymentResult = "PAID";
        try {
            paymentCommandService.autoCharge(order.getOrderId());
        } catch (BusinessException e) {
            log.warn("자동 결제 실패 orderId={} code={}", order.getOrderId(), e.getErrorCode());
            paymentResult = e.getErrorCode().name();
        }
        OrderDetailDto current = orderInternalQueryService.find(order.getOrderId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "주문 생성 성공");
        map.put("orderId", current.getOrderId());
        map.put("auctionId", current.getAuctionId());
        map.put("sellerId", current.getSellerId());
        map.put("buyerId", current.getBuyerId());
        map.put("finalPrice", current.getFinalPrice());
        map.put("status", current.getStatus());
        map.put("paymentDue", current.getPaymentDue());
        map.put("paymentResult", paymentResult);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
