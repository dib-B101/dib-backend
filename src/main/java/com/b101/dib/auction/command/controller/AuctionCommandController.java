package com.b101.dib.auction.command.controller;

import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.command.service.AuctionCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionCommandController {
    private final AuctionCommandService auctionCommandService;
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestHeader("X-Member-Id") Long memberId, @Valid @RequestBody CreateAuctionRequest request) {
        Long id = auctionCommandService.create(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "경매 생성 성공", "auctionId", id));
    }
    @PatchMapping("/{auctionId}")
    public ResponseEntity<Map<String, Object>> update(@RequestHeader("X-Member-Id") Long memberId, @PathVariable Long auctionId, @Valid @RequestBody UpdateAuctionRequest request) {
        auctionCommandService.update(memberId, auctionId, request);
        return ResponseEntity.ok(Map.of("message", "경매 수정 성공", "auctionId", auctionId));
    }
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> delete(@RequestHeader("X-Member-Id") Long memberId, @PathVariable Long auctionId) {
        auctionCommandService.delete(memberId, auctionId);
        return ResponseEntity.noContent().build();
    }
}
