package com.b101.dib.devtools;

import com.b101.dib.paymentMethod.domain.PaymentMethod;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 로컬 테스트 전용 — local 프로필에서만 빈이 뜬다. prod에는 이 경로 자체가 없다.
 */
@RestController
@Profile("local")
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevCardRegisterController {
    private final DevCardRegisterService devCardRegisterService;

    @PostMapping("/members/{memberId}/payment-methods/card")
    public ResponseEntity<Map<String, Object>> register(@PathVariable("memberId") Long memberId,
                                                        @RequestBody @Valid DevCardRegisterRequest request) {
        PaymentMethod pm = devCardRegisterService.register(memberId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "[DEV] 결제 수단 등록 성공 (카드번호 직접 발급)");
        map.put("paymentMethodId", pm.getPaymentMethodId());
        map.put("type", pm.getType());
        map.put("cardCompany", pm.getCardCompany());
        map.put("cardNumber", pm.getCardNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
