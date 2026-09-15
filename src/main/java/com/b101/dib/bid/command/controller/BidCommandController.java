package com.b101.dib.bid.command.controller;

import com.b101.dib.bid.command.dto.BidPlacedDto;
import com.b101.dib.bid.command.dto.PlaceBidRequest;
import com.b101.dib.bid.command.service.BidCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class BidCommandController {
    private final BidCommandService bidCommandService;

    // 소켓(PLACE_BID)과 같은 서비스. REST 는 폴백/테스트용
    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<Map<String, Object>> place(@RequestHeader("X-Member-Id") Long memberId,
                                                     @PathVariable("auctionId") Long auctionId,
                                                     @Valid @RequestBody PlaceBidRequest request) {
        BidPlacedDto placed = bidCommandService.place(auctionId, memberId, request.getAmount());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "입찰 성공");
        map.put("data", placed);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
