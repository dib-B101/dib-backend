package com.b101.dib.bid.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.command.dto.BidPlacedDto;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.common.util.Times;
import com.b101.dib.outboxEvent.command.service.OutboxEventRecorder;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// 입찰 트랜잭션(DB 행 락 안). 성패를 결정하는 쓰기만: bid INSERT, auction 갱신, outbox INSERT. 바깥 Redis 락은 BidCommandServiceImpl.
// OUTBID 알림·행동 로그는 dib.bid.placed Consumer 가, 소켓 브로드캐스트는 커밋 후 BidPlacedEvent 리스너가 한다
@Service
@RequiredArgsConstructor
@Transactional
public class BidTxServiceImpl implements BidTxService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final OutboxEventRecorder outboxEventRecorder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public BidPlacedDto place(Long auctionId, Long memberId, Long amount) {
        LocalDateTime now = LocalDateTime.now();
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)   // 같은 경매 입찰은 여기서 직렬화
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        if (!auction.isActiveAt(now)) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        Product product = productRepository.findById(auction.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.SELLER_CANNOT_BID);
        }
        Long previousTopBidderId = null;
        if (auction.getTopBidId() != null) {
            Bid top = bidRepository.findById(auction.getTopBidId()).orElse(null);
            if (top != null) {
                if (top.getMemberId().equals(memberId)) {
                    throw new BusinessException(ErrorCode.ALREADY_HIGHEST_BIDDER);
                }
                previousTopBidderId = top.getMemberId();
            }
        }
        if (amount < auction.minNextBid()) {
            throw new BusinessException(ErrorCode.BID_TOO_LOW);
        }

        boolean firstBidOfMember = !bidRepository.existsByAuctionIdAndMemberId(auctionId, memberId);
        Bid bid;
        try {
            bid = bidRepository.saveAndFlush(Bid.place(auctionId, memberId, amount));   // UNIQUE(auction_id, amount)
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.BID_AMOUNT_TAKEN);
        }
        boolean extended = auction.applyBid(bid.getBidId(), amount, firstBidOfMember, now);

        BidPlacedDto placed = new BidPlacedDto();
        placed.setBidId(bid.getBidId());
        placed.setAuctionId(auctionId);
        placed.setMemberId(memberId);
        placed.setAmount(amount);
        placed.setCurrentPrice(auction.getCurrentPrice());
        placed.setMinAllowedAmount(auction.minNextBid());
        placed.setBidCount(auction.getBidCount());
        placed.setBidderCount(auction.getBidderCount());
        placed.setEndedAt(Times.iso(auction.getEndedAt()));
        placed.setExtended(extended);
        placed.setExtensionCount(auction.getExtensionCount());
        placed.setHighestBidder(true);
        placed.setPreviousTopBidderId(previousTopBidderId);

        outboxEventRecorder.record("AUCTION", auctionId, "BID_PLACED", KafkaTopics.BID_PLACED, bidPlacedPayload(placed, product));
        eventPublisher.publishEvent(new BidPlacedEvent(placed));   // 커밋 후 소켓으로 (같은 Pod 리스너 → Redis 팬아웃)
        return placed;
    }

    private Map<String, Object> bidPlacedPayload(BidPlacedDto p, Product product) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("bidId", p.getBidId());
        payload.put("auctionId", p.getAuctionId());
        payload.put("productId", product.getProductId());
        payload.put("categoryId", product.getCategoryId());
        payload.put("sellerId", product.getMemberId());
        payload.put("memberId", p.getMemberId());
        payload.put("amount", p.getAmount());
        payload.put("previousTopBidderId", p.getPreviousTopBidderId());
        payload.put("currentPrice", p.getCurrentPrice());
        payload.put("minAllowedAmount", p.getMinAllowedAmount());
        payload.put("bidCount", p.getBidCount());
        payload.put("bidderCount", p.getBidderCount());
        payload.put("endedAt", p.getEndedAt());
        payload.put("extended", p.isExtended());
        payload.put("extensionCount", p.getExtensionCount());
        return payload;
    }
}
