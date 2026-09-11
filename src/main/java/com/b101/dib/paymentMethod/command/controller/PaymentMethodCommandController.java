package com.b101.dib.paymentMethod.command.controller;

import com.b101.dib.paymentMethod.command.dto.RegisterPaymentMethodRequest;
import com.b101.dib.paymentMethod.command.service.PaymentMethodCommandService;
import com.b101.dib.paymentMethod.domain.PaymentMethod;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/me/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodCommandController {
    private final PaymentMethodCommandService paymentMethodCommandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@RequestHeader("X-Member-Id") Long memberId,
                                                        @RequestBody @Valid RegisterPaymentMethodRequest request) {
        PaymentMethod pm = paymentMethodCommandService.register(memberId, request.authKey(), request.customerKey());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "결제 수단 등록 성공");
        map.put("paymentMethodId", pm.getPaymentMethodId());
        map.put("type", pm.getType());
        map.put("cardCompany", pm.getCardCompany());
        map.put("cardNumber", pm.getCardNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestHeader("X-Member-Id") Long memberId) {
        paymentMethodCommandService.delete(memberId);
        return ResponseEntity.noContent().build();
    }
}
