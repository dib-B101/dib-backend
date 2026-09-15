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
        map.put("paymentMethod", pm);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
