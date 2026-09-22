package com.b101.dib.auction.query.controller;

import com.b101.dib.auction.query.service.AuctionFeedQueryService;
import com.b101.dib.auction.query.service.AuctionQueryService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionQueryController {
    private final AuctionQueryService auctionQueryService;
    private final AuctionFeedQueryService auctionFeedQueryService;

    // 목록 (프론트 getAuctions / getGeneralAuctions). scope=ALL|LIVE|GENERAL, status=ACTIVE(기본)|SCHEDULED|ENDED|OPEN(진행+예정)|ALL,
    // sort=LATEST(기본)|ENDING_SOON|POPULAR|PRICE_ASC|PRICE_DESC|BID_COUNT. 응답 data: {items, nextCursor, hasNext}
    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "status", required = false) String status,
            // mine=true — 내가 판매자인 경매만. 라이브 편성 후보(scope=GENERAL&status=SCHEDULED&mine=true)가 쓴다
            @RequestParam(value = "mine", required = false, defaultValue = "false") boolean mine,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ok("경매 목록 조회 성공",
                auctionFeedQueryService.findCards(memberId, mine, scope, status, categoryId, minPrice, maxPrice, sort, cursor, size));
    }
    
    // 상세 (프론트 AuctionDto: product, sellerSummary, myBid, bookmarked, scheduledEndAt, serverTime 포함)
    @GetMapping("/{auctionId}")
    public ResponseEntity<Map<String, Object>> findById(
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @PathVariable("auctionId") Long auctionId){
        return ok("경매 상세 조회 성공", auctionFeedQueryService.findCard(memberId, auctionId));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<Map<String, Object>> findBySellerId(@PathVariable Long sellerId) {
        return ok("판매자 경매 조회 성공", auctionQueryService.findBySellerId(sellerId));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> findActive() {
        return ok("진행 중 경매 조회 성공", auctionQueryService.findActive());
    }
    
    // 홈 추천 (프론트 AuctionRecommendationResponse): data: {liveItems, generalItems, nextCursor, hasNext}
    @GetMapping("/recommendation")
    public ResponseEntity<Map<String, Object>> findRecommendations(
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(value = "size", defaultValue = "20") int size){
        return ok("추천 경매 목록 조회 성공", auctionFeedQueryService.recommend(memberId, size));
    }

    private ResponseEntity<Map<String, Object>> ok(String message, Object data) {
    	HashMap<String, Object> map = new HashMap<>();
    	map.put("message", message);
    	map.put("data", data);
        return ResponseEntity
        		.status(HttpStatus.OK)
        		.body(map);
    }
}
