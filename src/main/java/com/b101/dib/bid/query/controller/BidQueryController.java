package com.b101.dib.bid.query.controller;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.bid.query.service.BidQueryService;
import com.b101.dib.common.dto.CursorPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class BidQueryController {
    private final BidQueryService bidQueryService;

    @GetMapping("/auctions/{auctionId}/bids")
    public ResponseEntity<Map<String, Object>> findByAuction(@PathVariable("auctionId") Long auctionId,
                                                             @RequestParam(name = "cursor", required = false) String cursor,
                                                             @RequestParam(name = "size", defaultValue = "20") int size) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "입찰 내역 조회 성공");
        map.put("data", bidQueryService.findByAuctionId(auctionId, cursor, size));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    // 비로그인도 볼 수 있음. 로그인이면 isHighestBidder 계산
    @GetMapping("/auctions/{auctionId}/bid-snapshot")
    public ResponseEntity<Map<String, Object>> snapshot(@RequestHeader(value = "X-Member-Id", required = false) Long memberId,
                                                        @PathVariable("auctionId") Long auctionId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "경매 현황 조회 성공");
        map.put("data", bidQueryService.snapshot(auctionId, memberId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/members/me/bids")
    public ResponseEntity<CursorPageDto<MyBidQueryDto>> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(name = "status", required = false) AuctionStatus status,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                bidQueryService.findMine(claims.memberId(), status, cursor, size)
        );
    }
}
