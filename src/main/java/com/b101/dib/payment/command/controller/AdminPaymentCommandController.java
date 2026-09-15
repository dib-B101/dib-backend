package com.b101.dib.payment.command.controller;

import com.b101.dib.payment.command.dto.RefundRequest;
import com.b101.dib.payment.command.service.PaymentCommandService;
import com.b101.dib.payment.domain.Payment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentCommandController {
    private final PaymentCommandService paymentCommandService;

    @PostMapping("/{paymentId}/refunds")
    public ResponseEntity<Map<String, Object>> refund(@PathVariable("paymentId") Long paymentId,
                                                      @RequestBody @Valid RefundRequest request) {
        Payment payment = paymentCommandService.refund(paymentId, request.getReason(), request.getAmount());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "환불 처리 완료");
        HashMap<String, Object> data = new HashMap<>();
        data.put("payment", payment);
        data.put("refundKey", payment.getRefundKey());
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(map);
    }
}
