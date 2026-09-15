package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.payment.command.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 마감 지난 ACTIVE 경매를 1초마다 찾아 종료 → 낙찰자 주문 생성 → 자동결제 → 소켓 AUCTION_ENDED
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionEndScheduler {
    private final AuctionRepository auctionRepository;
    private final AuctionEndTxService auctionEndTxService;
    private final PaymentCommandService paymentCommandService;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(fixedDelay = 1_000)
    public void run() {
        List<Auction> due = auctionRepository.findAllByStatusAndEndedAtBefore(AuctionStatus.ACTIVE, LocalDateTime.now());
        for (Auction auction : due) {
            try {
                AuctionEndResultDto result = auctionEndTxService.endOne(auction.getAuctionId());
                if (result == null) {
                    continue;
                }
                if (result.getOrderId() != null) {
                    autoCharge(result.getOrderId());
                }
                eventPublisher.publishEvent(new AuctionEndedEvent(result));
                log.info("경매 종료 auctionId={} result={} orderId={}", result.getAuctionId(), result.getResult(), result.getOrderId());
            } catch (Exception e) {
                log.error("경매 종료 실패 auctionId={}", auction.getAuctionId(), e);
            }
        }
    }

    private void autoCharge(Long orderId) {
        try {
            paymentCommandService.autoCharge(orderId);   // 실패해도 주문은 PENDING 으로 남고 구매자가 재시도 (OrderInternalController 와 동일)
        } catch (BusinessException e) {
            log.warn("낙찰 자동결제 실패 orderId={} code={}", orderId, e.getErrorCode());
        }
    }
}
