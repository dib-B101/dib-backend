package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.command.entity.Auction;
import com.b101.dib.auction.command.repository.AuctionCommandRepository;
import com.b101.dib.auction.query.dto.AuctionStatus;
import com.b101.dib.common.exception.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionCommandServiceImpl implements AuctionCommandService {
    private final AuctionCommandRepository repository;

    public Long create(Long memberId, CreateAuctionRequest r) {
        Auction auction = Auction.builder()
                .memberId(memberId)
                .productId(r.getProductId())
                .categoryId(r.getCategoryId())
                .startPrice(r.getStartPrice())
                .currentPrice(r.getStartPrice())
                .auctionTime(r.getAuctionTime())
                .depositAmount(r.getDepositAmount())
                .liveBroadcastId(r.getLiveBroadcastId())
                .status(AuctionStatus.SCHEDULED)
                .bidCount(0).bidderCount(0).viewCount(0).bookmarkCount(0).extensionCount(0)
                .createdAt(LocalDateTime.now())
                .build();
        return repository.save(auction).getAuctionId();
    }

    public void update(Long memberId, Long auctionId, UpdateAuctionRequest r) {
        Auction a = editable(memberId, auctionId);
        if (r.getCategoryId() != null) a.setCategoryId(r.getCategoryId());
        if (r.getStartPrice() != null) {
            a.setStartPrice(r.getStartPrice());
            a.setCurrentPrice(r.getStartPrice());
        }
        if (r.getAuctionTime() != null) a.setAuctionTime(r.getAuctionTime());
        if (r.getStartedAt() != null) a.setStartedAt(r.getStartedAt());
        if (r.getEndedAt() != null) a.setEndedAt(r.getEndedAt());
        if (r.getDepositAmount() != null) a.setDepositAmount(r.getDepositAmount());
        if (r.getLiveBroadcastId() != null) a.setLiveBroadcastId(r.getLiveBroadcastId());
        a.setUpdatedAt(LocalDateTime.now());
    }

    public void delete(Long memberId, Long auctionId) {
        Auction a = editable(memberId, auctionId);
        a.setDeletedAt(LocalDateTime.now());
        a.setStatus(AuctionStatus.CANCELLED);
    }

    private Auction editable(Long memberId, Long auctionId) {
        Auction a = repository.findById(auctionId).orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        if (a.getDeletedAt() != null) throw new BusinessException(ErrorCode.AUCTION_ALREADY_DELETED);
        if (!a.getMemberId().equals(memberId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (a.getStatus() != AuctionStatus.SCHEDULED) throw new BusinessException(ErrorCode.AUCTION_NOT_EDITABLE);
        return a;
    }
}
