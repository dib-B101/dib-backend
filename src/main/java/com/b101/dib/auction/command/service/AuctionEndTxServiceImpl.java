package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.common.util.Times;
import com.b101.dib.order.command.service.OrderCommandService;
import com.b101.dib.order.domain.Order;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// 종료 트랜잭션: 경매 ENDED, 상품 상태, 낙찰 주문 생성, outbox(AUCTION_CLOSED).
// 자동결제·낙찰 알림·이상입찰 분석은 dib.auction.closed Consumer 가 한다
@Service
@RequiredArgsConstructor
@Transactional
public class AuctionEndTxServiceImpl implements AuctionEndTxService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final OrderCommandService orderCommandService;
    private final OutboxEventRecorder outboxEventRecorder;

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
            outboxEventRecorder.record("AUCTION", auctionId, "AUCTION_CLOSED", KafkaTopics.AUCTION_CLOSED, closedPayload(auction, product, result));
            return result;
        }

        Order order = orderCommandService.create(auctionId);   // 낙찰자 PENDING 주문 (낙찰 결과의 일부라 여기서). 결제는 Consumer 가
        Bid top = bidRepository.findById(auction.getTopBidId()).orElse(null);
        Long winnerId = top != null ? top.getMemberId() : order.getBuyerId();
        if (product != null) {
            product.setStatus(ProductStatus.SOLD);
            product.setUpdatedAt(now);
        }
        result.setResult("SOLD");
        result.setWinnerId(winnerId);
        result.setOrderId(order.getOrderId());
        outboxEventRecorder.record("AUCTION", auctionId, "AUCTION_CLOSED", KafkaTopics.AUCTION_CLOSED, closedPayload(auction, product, result));
        return result;
    }

    private Map<String, Object> closedPayload(Auction auction, Product product, AuctionEndResultDto r) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", auction.getAuctionId());
        payload.put("productId", auction.getProductId());
        payload.put("sellerId", product == null ? null : product.getMemberId());
        payload.put("result", r.getResult());
        payload.put("finalPrice", r.getFinalPrice());
        payload.put("winnerId", r.getWinnerId());
        payload.put("topBidId", auction.getTopBidId());
        payload.put("orderId", r.getOrderId());
        payload.put("bidCount", auction.getBidCount());
        payload.put("bidderCount", auction.getBidderCount());
        payload.put("extensionCount", auction.getExtensionCount());
        payload.put("startedAt", Times.iso(auction.getStartedAt()));
        payload.put("endedAt", r.getEndedAt());
        payload.put("liveBroadcastId", auction.getLiveBroadcastId());
        return payload;
    }
}
