package com.b101.dib.auction.query.controller;

import com.b101.dib.auction.query.dto.AuctionQueryDto;
import com.b101.dib.auction.query.service.AuctionQueryService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionQueryController {
    private final AuctionQueryService auctionQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        return ok("경매 전체 조회 성공", auctionQueryService.findAll());
    }
    
    @GetMapping("/{auctionId}")
    public ResponseEntity<Map<String, Object>> findById(
    		@PathVariable("auctionId") Long auctionId){
    	return ok("경매 상세 조회 성공", auctionQueryService.findById(auctionId));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<Map<String, Object>> findBySellerId(@PathVariable Long sellerId) {
        return ok("판매자 경매 조회 성공", auctionQueryService.findBySellerId(sellerId));
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> findActive() {
        return ok("진행 중 경매 조회 성공", auctionQueryService.findActive());
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
