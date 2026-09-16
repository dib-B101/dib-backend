package com.b101.dib.bid.query.service;

import com.b101.dib.auction.command.service.AuctionEndedEvent;
import com.b101.dib.bid.command.service.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

// 입찰 커밋·경매 종료 뒤 Redis Hot State 를 DB 기준으로 갱신 (아키텍처 "COMMIT → Redis Hot State 갱신")
@Component
@RequiredArgsConstructor
@Slf4j
public class BidSnapshotCacheRefresher {
    private final BidQueryService bidQueryService;

    @TransactionalEventListener
    public void onBidPlaced(BidPlacedEvent event) {
        refresh(event.getPlaced().getAuctionId());
    }

    @EventListener
    public void onAuctionEnded(AuctionEndedEvent event) {
        refresh(event.getResult().getAuctionId());
    }

    private void refresh(Long auctionId) {
        try {
            bidQueryService.refreshSnapshot(auctionId);
        } catch (Exception e) {
            log.warn("스냅샷 갱신 실패 auctionId={} : {}", auctionId, e.getMessage());
        }
    }
}
