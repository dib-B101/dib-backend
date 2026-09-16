package com.b101.dib.auction.query.controller;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.SaleHistoryQueryDto;
import com.b101.dib.auction.query.service.MemberSaleQueryService;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.common.dto.CursorPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/sales")
@RequiredArgsConstructor
public class MemberSaleQueryController {
    private final MemberSaleQueryService memberSaleQueryService;

    @GetMapping
    public ResponseEntity<CursorPageDto<SaleHistoryQueryDto>> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(name = "auctionStatus", required = false) AuctionStatus auctionStatus,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                memberSaleQueryService.findMine(
                        claims.memberId(),
                        auctionStatus,
                        cursor,
                        size
                )
        );
    }
}
