package com.b101.dib.auction.query.controller;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.SaleHistoryQueryDto;
import com.b101.dib.auction.query.service.MemberSaleQueryService;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.common.dto.CursorPageDto;
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
@RequestMapping("/api/v1/members/me/sales")
@RequiredArgsConstructor
public class MemberSaleQueryController {
    private final MemberSaleQueryService memberSaleQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(name = "auctionStatus", required = false) AuctionStatus auctionStatus,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        // 다른 컨트롤러와 같은 {message, data} 봉투로 맞춘다. 이 컨트롤러만 페이지 객체를 날것으로 주고 있었다
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "판매 내역 조회 성공");
        map.put("data", memberSaleQueryService.findMine(
                claims.memberId(),
                auctionStatus,
                cursor,
                size
        ));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
