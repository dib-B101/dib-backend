package com.b101.dib.payment.query.controller;

import com.b101.dib.payment.query.service.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentQueryController {
    private final PaymentQueryService paymentQueryService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> findDetail(@RequestHeader("X-Member-Id") Long memberId,
                                                          @PathVariable("paymentId") Long paymentId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "결제 조회 성공");
        map.put("data", paymentQueryService.findDetail(memberId, paymentId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
