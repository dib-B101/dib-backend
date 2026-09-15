package com.b101.dib.bid.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.command.dto.BidPlacedDto;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.Times;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class BidCommandServiceImpl implements BidCommandService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;
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

        if (previousTopBidderId != null) {
            notificationRepository.save(Notification.outbid(auctionId, previousTopBidderId, bid.getBidId(), amount));
        }

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
        eventPublisher.publishEvent(new BidPlacedEvent(placed));   // 커밋 후 소켓으로
        return placed;
    }
}
