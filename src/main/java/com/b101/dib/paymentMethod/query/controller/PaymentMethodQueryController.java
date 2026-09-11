package com.b101.dib.paymentMethod.query.controller;

import com.b101.dib.paymentMethod.query.service.PaymentMethodQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/me/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodQueryController {
    private final PaymentMethodQueryService paymentMethodQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(@RequestHeader("X-Member-Id") Long memberId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "결제 수단 조회 성공");
        map.put("data", paymentMethodQueryService.findMine(memberId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
