package com.b101.dib.review.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 구매확정 직후 구매자에게 "별점 남겨 주세요" 알림을 보낸다.
// 확정 경로가 둘(구매자가 직접 / 배송완료 후 자동)이라 한곳에 모았다.
@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewRequestNotifier {

    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;

    public void requestFor(Order order) {
        if (order == null || order.getBuyerId() == null) {
            return;
        }
        // 확정 전에 이미 평가했을 수는 없지만, 재확정 같은 경로로 두 번 불려도 알림이 겹치지 않게 막는다
        if (reviewRepository.existsByOrderId(order.getOrderId())) {
            return;
        }
        try {
            notificationRepository.save(
                    Notification.reviewRequest(order.getOrderId(), order.getBuyerId(), productTitleOf(order)));
        } catch (RuntimeException e) {
            // 평가 요청을 못 보냈다고 구매확정·정산을 되돌릴 이유는 없다
            log.warn("평가 요청 알림 저장 실패 orderId={}", order.getOrderId(), e);
        }
    }

    private String productTitleOf(Order order) {
        if (order.getAuctionId() == null) {
            return "거래한 상품";
        }
        Auction auction = auctionRepository.findById(order.getAuctionId()).orElse(null);
        if (auction == null || auction.getProductId() == null) {
            return "거래한 상품";
        }
        Product product = productRepository.findById(auction.getProductId()).orElse(null);
        return product == null || product.getTitle() == null ? "거래한 상품" : product.getTitle();
    }
}
