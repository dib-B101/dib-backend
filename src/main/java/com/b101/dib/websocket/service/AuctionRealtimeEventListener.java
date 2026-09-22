package com.b101.dib.websocket.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.command.service.AuctionEndedEvent;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.command.dto.BidPlacedDto;
import com.b101.dib.bid.command.service.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

// 입찰/종료 트랜잭션이 커밋된 뒤에만 구독자에게 뿌린다
@Component
@RequiredArgsConstructor
public class AuctionRealtimeEventListener {
    private final AuctionWebSocketService auctionWebSocketService;
    private final AuctionRepository auctionRepository;
    private final LiveWebSocketService liveWebSocketService;

    @TransactionalEventListener
    public void onBidPlaced(BidPlacedEvent event) {
        BidPlacedDto p = event.getPlaced();
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", String.valueOf(p.getAuctionId()));
        payload.put("currentPrice", p.getCurrentPrice());
        payload.put("minAllowedAmount", p.getMinAllowedAmount());
        payload.put("bidCount", p.getBidCount());
        payload.put("bidderCount", p.getBidderCount());
        payload.put("endedAt", p.getEndedAt());
        auctionWebSocketService.broadcast(p.getAuctionId(), "HIGHEST_BID_UPDATED", payload);

        if (p.isExtended()) {
            Map<String, Object> ext = new HashMap<>();
            ext.put("auctionId", String.valueOf(p.getAuctionId()));
            ext.put("endedAt", p.getEndedAt());
            ext.put("extensionSeconds", Auction.EXTEND_WINDOW_SECONDS);
            ext.put("extensionCount", p.getExtensionCount());
            auctionWebSocketService.broadcast(p.getAuctionId(), "AUCTION_EXTENDED", ext);
        }
    }

    // 스케줄러는 트랜잭션 밖에서 발행하므로 일반 리스너
    @EventListener
    public void onAuctionEnded(AuctionEndedEvent event) {
        AuctionEndResultDto r = event.getResult();
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", String.valueOf(r.getAuctionId()));
        payload.put("result", r.getResult());
        payload.put("finalPrice", r.getFinalPrice());
        payload.put("orderId", r.getOrderId() == null ? null : String.valueOf(r.getOrderId()));
        payload.put("winnerId", r.getWinnerId() == null ? null : String.valueOf(r.getWinnerId()));
        payload.put("endedAt", r.getEndedAt());
        auctionWebSocketService.broadcast(r.getAuctionId(), "AUCTION_ENDED", payload);

        // 라이브에 편성된 경매면 방송 화면도 같은 종료를 봐야 한다
        Long liveBroadcastId = auctionRepository.findById(r.getAuctionId())
                .map(Auction::getLiveBroadcastId)
                .orElse(null);
        if (liveBroadcastId != null) {
            Map<String, Object> live = new HashMap<>();
            live.put("liveBroadcastId", String.valueOf(liveBroadcastId));
            live.put("auctionId", String.valueOf(r.getAuctionId()));
            live.put("finalPrice", r.getFinalPrice());
            live.put("result", r.getResult());
            liveWebSocketService.broadcast(liveBroadcastId, "LIVE_AUCTION_CLOSED", live);
        }
    }
}
