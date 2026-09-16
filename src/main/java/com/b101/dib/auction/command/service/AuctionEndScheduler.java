package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.lock.AuctionLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 마감 지난 ACTIVE 경매를 1초마다 찾아 (Redis 락 → 종료 트랜잭션) → 소켓 AUCTION_ENDED.
// 자동결제(외부 API)·알림·이상탐지는 여기서 하지 않고 outbox → Kafka Consumer 로 넘어간다 (스케줄러 스레드가 외부 API 를 기다리지 않게)
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEndScheduler {
    private final AuctionRepository auctionRepository;
    private final AuctionEndTxService auctionEndTxService;
    private final AuctionLock auctionLock;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 1_000)
    public void run() {
        List<Auction> due = auctionRepository.findAllByStatusAndEndedAtBefore(AuctionStatus.ACTIVE, LocalDateTime.now());
        for (Auction auction : due) {
            Long auctionId = auction.getAuctionId();
            try {
                AuctionEndResultDto result = auctionLock.run(auctionId, () -> auctionEndTxService.endOne(auctionId));
                if (result == null) {
                    continue;
                }
                eventPublisher.publishEvent(new AuctionEndedEvent(result));
                log.info("경매 종료 auctionId={} result={} orderId={}", result.getAuctionId(), result.getResult(), result.getOrderId());
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.AUCTION_BUSY) {
                    log.error("경매 종료 실패 auctionId={} code={}", auctionId, e.getErrorCode());
                }
                // AUCTION_BUSY: 마감 직전 입찰이 락을 잡고 있음 — 다음 폴링에서 다시
            } catch (Exception e) {
                log.error("경매 종료 실패 auctionId={}", auctionId, e);
            }
        }
    }
}
