package com.b101.dib.order.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderMapper;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.review.command.service.ReviewRequestNotifier;
import com.b101.dib.settlement.command.service.SettlementCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderExpiryTxServiceImpl implements OrderExpiryTxService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final AuctionRepository auctionRepository;
    private final SettlementCommandService settlementCommandService;
    private final ReviewRequestNotifier reviewRequestNotifier;

    @Override
    public void expireOne(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PENDING
                || !order.getPaymentDue().isBefore(LocalDateTime.now())) {
            return;
        }
        order.expire();
        memberRepository.findById(order.getBuyerId()).ifPresent(this::addWarning);
        notificationRepository.save(Notification.order(orderId, order.getBuyerId(), "주문 취소",
                "주문 #" + orderId + "의 결제 기한이 지나 주문이 취소되었습니다. 경고 1회가 부여됩니다."));
        offerNext(order.getAuctionId());
    }

    @Override
    public void confirmOne(Long orderId, LocalDateTime deliveredBefore) {
        Order order = orderRepository.findById(orderId).orElse(null);
        // 보류 건은 조용히 건너뛴다. order.confirm()이 던지는 예외에 맡기면 매분 에러 로그만 쌓인다
        if (order == null || order.isOnHold() || order.getStatus() != OrderStatus.DELIEVERED
                || order.getUpdatedAt() == null || !order.getUpdatedAt().isBefore(deliveredBefore)) {
            return;
        }
        order.confirm();
        settlementCommandService.createFor(order);
        notificationRepository.save(Notification.order(orderId, order.getBuyerId(), "자동 구매 확정",
                "주문 #" + orderId + " 배송 완료 후 기간이 지나 자동으로 구매가 확정되었습니다."));
        reviewRequestNotifier.requestFor(order);
        log.info("자동 구매 확정 orderId={}", orderId);
    }

    @Override
    public void offerNext(Long auctionId) {
        Long runnerUp = orderMapper.findRunnerUpId(auctionId);
        if (runnerUp == null) {
            auctionRepository.findById(auctionId).ifPresent(this::endAuction);
            log.info("차순위 없음 → 경매 종료 auctionId={}", auctionId);
            return;
        }
        Long amount = orderMapper.findMaxBidAmount(auctionId, runnerUp);
        notificationRepository.save(Notification.offer(auctionId, runnerUp, amount));
        log.info("차순위 제안 auctionId={} memberId={} amount={}", auctionId, runnerUp, amount);
    }

    private void addWarning(Member member) {
        member.setWarningCount(member.getWarningCount() + 1);
    }

    private void endAuction(Auction auction) {
        if (auction.getStatus() != AuctionStatus.ENDED) {
            auction.setStatus(AuctionStatus.ENDED);
            auction.setUpdatedAt(LocalDateTime.now());
        }
    }
}
