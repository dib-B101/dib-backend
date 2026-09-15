package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.util.Times;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.command.service.OrderCommandService;
import com.b101.dib.order.domain.Order;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionEndTxServiceImpl implements AuctionEndTxService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;
    private final OrderCommandService orderCommandService;

    @Override
    public AuctionEndResultDto endOne(Long auctionId) {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionRepository.findByIdForUpdate(auctionId).orElse(null);
        if (auction == null || auction.getStatus() != AuctionStatus.ACTIVE) {
            return null;   // 다른 Pod 이 먼저 종료함
        }
        if (auction.getEndedAt() != null && auction.getEndedAt().isAfter(now)) {
            return null;   // 마감 직전 입찰로 시간이 리셋됨 — 다음 폴링에서
        }
        auction.end(now);
        Product product = productRepository.findById(auction.getProductId()).orElse(null);

        AuctionEndResultDto result = new AuctionEndResultDto();
        result.setAuctionId(auctionId);
        result.setFinalPrice(auction.getCurrentPrice());
        result.setEndedAt(Times.iso(auction.getEndedAt()));

        if (auction.getTopBidId() == null) {
            result.setResult("UNSOLD");
            if (product != null) {
                product.setStatus(ProductStatus.REGISTERED);   // 유찰 → 다시 경매 올릴 수 있게
                product.setUpdatedAt(now);
            }
            return result;
        }

        Order order = orderCommandService.create(auctionId);   // 낙찰자 PENDING 주문. 자동결제는 트랜잭션 밖(스케줄러)에서
        Bid top = bidRepository.findById(auction.getTopBidId()).orElse(null);
        Long winnerId = top != null ? top.getMemberId() : order.getBuyerId();
        notificationRepository.save(Notification.won(auctionId, winnerId, auction.getTopBidId(), auction.getCurrentPrice()));
        if (product != null) {
            product.setStatus(ProductStatus.SOLD);
            product.setUpdatedAt(now);
        }
        result.setResult("SOLD");
        result.setWinnerId(winnerId);
        result.setOrderId(order.getOrderId());
        return result;
    }
}
