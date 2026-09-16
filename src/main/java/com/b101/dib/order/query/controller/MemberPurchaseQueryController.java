package com.b101.dib.order.query.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.PurchaseHistoryQueryDto;
import com.b101.dib.order.query.service.MemberPurchaseQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/purchases")
@RequiredArgsConstructor
public class MemberPurchaseQueryController {
    private final MemberPurchaseQueryService memberPurchaseQueryService;

    @GetMapping
    public ResponseEntity<CursorPageDto<PurchaseHistoryQueryDto>> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(name = "orderStatus", required = false) OrderStatus orderStatus,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                memberPurchaseQueryService.findMine(
                        claims.memberId(),
                        orderStatus,
                        cursor,
                        size
                )
        );
    }
}
