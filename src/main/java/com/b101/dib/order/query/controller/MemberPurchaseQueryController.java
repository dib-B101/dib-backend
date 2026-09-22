package com.b101.dib.order.query.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.PurchaseHistoryQueryDto;
import com.b101.dib.order.query.service.MemberPurchaseQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/me/purchases")
@RequiredArgsConstructor
public class MemberPurchaseQueryController {
    private final MemberPurchaseQueryService memberPurchaseQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(name = "orderStatus", required = false) OrderStatus orderStatus,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        // 다른 컨트롤러와 같은 {message, data} 봉투로 맞춘다. 이 컨트롤러만 페이지 객체를 날것으로 주고 있었다
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "구매 내역 조회 성공");
        map.put("data", memberPurchaseQueryService.findMine(
                claims.memberId(),
                orderStatus,
                cursor,
                size
        ));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
