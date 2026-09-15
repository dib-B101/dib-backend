package com.b101.dib.websocket.controller;

import com.b101.dib.bid.command.dto.BidPlacedDto;
import com.b101.dib.bid.command.service.BidCommandService;
import com.b101.dib.bid.query.dto.BidSnapshotDto;
import com.b101.dib.bid.query.service.BidQueryService;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.Times;
import com.b101.dib.websocket.dto.PlaceBidCommand;
import com.b101.dib.websocket.dto.SocketEnvelope;
import com.b101.dib.websocket.service.AuctionWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

// STOMP. 구독 /topic/auctions/{id} (브로드캐스트), /user/queue/auction (내 입찰 결과)
//        전송 /app/auctions/{id}/bids {commandId, amount}
//        스냅샷 /app/auctions/{id}/snapshot 구독하면 1회 응답
@Controller
@RequiredArgsConstructor
public class AuctionWebsocketController {
    private final BidCommandService bidCommandService;
    private final BidQueryService bidQueryService;
    private final AuctionWebSocketService auctionWebSocketService;

    @MessageMapping("/auctions/{auctionId}/bids")
    public void placeBid(@DestinationVariable("auctionId") Long auctionId, PlaceBidCommand command, Principal principal) {
        Long memberId = memberId(principal);
        if (memberId == null) {
            return;   // CONNECT 에서 인증 안 된 세션 — 개인 큐도 없으니 무시
        }
        try {
            BidPlacedDto placed = bidCommandService.place(auctionId, memberId, command.getAmount());
            Map<String, Object> payload = new HashMap<>();
            payload.put("commandId", command.getCommandId());
            payload.put("bidId", String.valueOf(placed.getBidId()));
            payload.put("auctionId", String.valueOf(auctionId));
            payload.put("amount", placed.getAmount());
            payload.put("newCurrentPrice", placed.getCurrentPrice());
            payload.put("isHighestBidder", true);
            payload.put("endedAt", placed.getEndedAt());
            payload.put("acceptedAt", Times.now());
            auctionWebSocketService.sendToMember(memberId, "BID_ACCEPTED", command.getCommandId(), payload);
        } catch (BusinessException e) {
            sendRejected(memberId, auctionId, command.getCommandId(), e.getErrorCode());
        }
    }

    @SubscribeMapping("/auctions/{auctionId}/snapshot")
    public SocketEnvelope snapshot(@DestinationVariable("auctionId") Long auctionId, Principal principal) {
        BidSnapshotDto s = bidQueryService.snapshot(auctionId, memberId(principal));
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", String.valueOf(auctionId));
        payload.put("status", s.getStatus() == null ? null : s.getStatus().name());
        payload.put("currentPrice", s.getCurrentPrice());
        payload.put("minAllowedAmount", s.getMinAllowedAmount());
        payload.put("bidCount", s.getBidCount());
        payload.put("bidderCount", s.getBidderCount());
        payload.put("endedAt", s.getScheduledEndAt());
        payload.put("serverTime", s.getServerTime());
        Map<String, Object> myBid = new HashMap<>();
        myBid.put("isHighestBidder", s.isHighestBidder());
        payload.put("myBid", myBid);
        return SocketEnvelope.of("AUCTION_SNAPSHOT", null, payload);
    }

    private void sendRejected(Long memberId, Long auctionId, String commandId, ErrorCode code) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("commandId", commandId);
        payload.put("auctionId", String.valueOf(auctionId));
        payload.put("code", code.name());
        payload.put("message", code.getMessage());
        try {
            BidSnapshotDto s = bidQueryService.snapshot(auctionId, memberId);
            payload.put("currentPrice", s.getCurrentPrice());
            payload.put("minAllowedAmount", s.getMinAllowedAmount());
            payload.put("endedAt", s.getScheduledEndAt());
        } catch (BusinessException ignored) {
            // 경매가 없으면 현황 없이 보낸다
        }
        payload.put("rejectedAt", Times.now());
        auctionWebSocketService.sendToMember(memberId, "BID_REJECTED", commandId, payload);
    }

    private static Long memberId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
