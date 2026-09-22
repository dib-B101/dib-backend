package com.b101.dib.bid.query.service;

import com.b101.dib.auction.command.service.AuctionEndedEvent;
import com.b101.dib.auction.command.service.AuctionStateChangedEvent;
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

    // 시작·조건 수정·재등록·취소. DB 를 다시 시드해 auctionId 가 재사용돼도 예전 경매 스냅샷이 남지 않게 커밋 뒤 덮어쓴다
    @TransactionalEventListener
    public void onAuctionStateChanged(AuctionStateChangedEvent event) {
        refresh(event.getAuctionId());
    }

    private void refresh(Long auctionId) {
        try {
            bidQueryService.refreshSnapshot(auctionId);
        } catch (Exception e) {
            log.warn("스냅샷 갱신 실패 auctionId={} : {}", auctionId, e.getMessage());
        }
    }
}
