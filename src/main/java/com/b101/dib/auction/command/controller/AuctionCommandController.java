package com.b101.dib.auction.command.controller;

import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.command.service.AuctionCommandService;
import com.b101.dib.auction.domain.Auction;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionCommandController {
	
    private final AuctionCommandService auctionCommandService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateAuctionRequest request) {
    	Long myId = 1L;
        Auction auction = auctionCommandService.create(myId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "경매 생성 성공");
        map.put("auction", auction);
        return ResponseEntity
        		.status(HttpStatus.CREATED)
        		.body(map);
    }
    
    @PatchMapping("/{auctionId}")
    public ResponseEntity<Map<String, Object>> update(
    		@PathVariable("auctionId") Long auctionId,
    		@RequestBody UpdateAuctionRequest request
    		) {
    	Long myId = 1L;
        Auction auction = auctionCommandService.update(myId, auctionId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "경매 수정 성공");
        map.put("auction", auction);
        return ResponseEntity
        		.status(HttpStatus.OK)
        		.body(map);
    }
    
    @PatchMapping("/{auctionId}/start")
    public ResponseEntity<Map<String, Object>> startAuction(
    		@PathVariable("auctionId") Long auctionId
    		){
    	Long myId = 1L;
    	Auction auction = auctionCommandService.startAuction(myId, auctionId);
    	HashMap<String, Object> map = new HashMap<>();
        map.put("message", "경매 시작");
        map.put("auction", auction);
        return ResponseEntity
        		.status(HttpStatus.OK)
        		.body(map);
    }
    
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Map<String, Object>> delete(
    		@PathVariable("auctionId") Long auctionId
    		) {
    	Long myId = 1L;
        Auction auction = auctionCommandService.delete(myId, auctionId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "경매 삭제 성공");
        map.put("data", auction);
        return ResponseEntity
        		.status(HttpStatus.OK)
        		.body(map);
    }
}
