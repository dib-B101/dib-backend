package com.b101.dib.payment.command.controller;

import com.b101.dib.payment.command.service.PaymentCommandService;
import com.b101.dib.payment.domain.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class PaymentCommandController {
    private final PaymentCommandService paymentCommandService;

    @PostMapping("/{orderId}/payments/retry")
    public ResponseEntity<Map<String, Object>> retry(@RequestHeader("X-Member-Id") Long memberId,
                                                     @PathVariable("orderId") Long orderId) {
        Payment payment = paymentCommandService.retry(memberId, orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "결제 성공");
        map.put("paymentId", payment.getPaymentId());
        map.put("orderId", payment.getOrderId());
        map.put("amount", payment.getAmount());
        map.put("type", payment.getType());
        map.put("receiptUrl", payment.getReceiptUrl());
        map.put("paidAt", payment.getPaidAt());
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
