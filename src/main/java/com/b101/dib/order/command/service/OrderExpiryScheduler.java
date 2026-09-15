package com.b101.dib.order.command.service;

import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderMapper;
import com.b101.dib.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryScheduler {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderExpiryTxService orderExpiryTxService;

    @Value("${dib.order.auto-confirm-days:5}")
    private int autoConfirmDays;

    @Scheduled(fixedDelay = 60_000)
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        List<Order> overdue = orderRepository.findAllByStatusAndPaymentDueBefore(OrderStatus.PENDING, now);
        for (Order o : overdue) {
            try {
                orderExpiryTxService.expireOne(o.getOrderId());
            } catch (Exception e) {
                log.error("결제 기한 만료 처리 실패 orderId={}", o.getOrderId(), e);
            }
        }
        List<Long> expiredOffers = orderMapper.findExpiredOfferAuctionIds(now.minusHours(OrderOfferServiceImpl.OFFER_HOURS));
        for (Long auctionId : expiredOffers) {
            try {
                orderExpiryTxService.offerNext(auctionId);
            } catch (Exception e) {
                log.error("차순위 제안 만료 처리 실패 auctionId={}", auctionId, e);
            }
        }
        LocalDateTime deliveredBefore = now.minusDays(autoConfirmDays);
        List<Order> unconfirmed = orderRepository.findAllByStatusAndUpdatedAtBefore(OrderStatus.DELIEVERED, deliveredBefore);
        for (Order o : unconfirmed) {
            try {
                orderExpiryTxService.confirmOne(o.getOrderId(), deliveredBefore);
            } catch (Exception e) {
                log.error("자동 구매 확정 실패 orderId={}", o.getOrderId(), e);
            }
        }
    }
}
