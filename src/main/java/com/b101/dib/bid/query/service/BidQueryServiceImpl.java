package com.b101.dib.bid.query.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.query.dto.BidHistoryQueryDto;
import com.b101.dib.bid.query.dto.BidSnapshotCacheEntry;
import com.b101.dib.bid.query.dto.BidSnapshotDto;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.bid.repository.BidMapper;
import com.b101.dib.bid.repository.BidRepository;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.Times;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidQueryServiceImpl implements BidQueryService {
    private final BidMapper bidMapper;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final BidSnapshotCache bidSnapshotCache;

    @Override
    public CursorPageDto<BidHistoryQueryDto> findByAuctionId(Long auctionId, String cursor, int size) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_FOUND);
        }
        int limit = CursorPageDto.limit(size);
        List<BidHistoryQueryDto> rows = bidMapper.findByAuctionId(auctionId, CursorPageDto.parseCursor(cursor), limit + 1);
        for (BidHistoryQueryDto row : rows) {
            row.setMaskedBidderId(mask(row.getBidderId()));
        }
        return CursorPageDto.of(rows, limit, BidHistoryQueryDto::getBidId);
    }

    @Override
    public CursorPageDto<MyBidQueryDto> findMine(
            Long memberId,
            AuctionStatus status,
            String cursor,
            int size
    ) {
        int limit = CursorPageDto.limit(size);
        List<MyBidQueryDto> rows = bidMapper.findByMemberId(
                memberId,
                status,
                CursorPageDto.parseCursor(cursor),
                limit + 1
        );
        return CursorPageDto.of(rows, limit, MyBidQueryDto::getBidId);
    }

    // Cache-Aside: Redis hit 면 DB 를 안 친다. miss 면 DB → Redis 저장
    @Override
    public BidSnapshotDto snapshot(Long auctionId, Long memberId) {
        BidSnapshotCacheEntry entry = bidSnapshotCache.get(auctionId).orElseGet(() -> loadAndCache(auctionId));
        BidSnapshotDto dto = entry.getSnapshot();
        dto.setHighestBidder(memberId != null && memberId.equals(entry.getTopBidderId()));
        dto.setServerTime(Times.now());
        return dto;
    }

    @Override
    public void refreshSnapshot(Long auctionId) {
        if (auctionRepository.existsById(auctionId)) {
            loadAndCache(auctionId);
        }
    }

    private BidSnapshotCacheEntry loadAndCache(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        Long topBidderId = null;
        if (auction.getTopBidId() != null) {
            Bid top = bidRepository.findById(auction.getTopBidId()).orElse(null);
            topBidderId = top == null ? null : top.getMemberId();
        }
        BidSnapshotDto dto = new BidSnapshotDto();
        dto.setAuctionId(auction.getAuctionId());
        dto.setStatus(auction.getStatus());
        dto.setStartPrice(auction.getStartPrice());
        dto.setCurrentPrice(auction.getCurrentPrice());
        dto.setMinAllowedAmount(auction.minNextBid());
        dto.setAuctionTime(auction.getAuctionTime());
        dto.setStartedAt(auction.getStartedAt());
        dto.setScheduledEndAt(Times.iso(auction.getEndedAt()));
        dto.setBidCount(auction.getBidCount());
        dto.setBidderCount(auction.getBidderCount());
        dto.setExtensionCount(auction.getExtensionCount());

        BidSnapshotCacheEntry entry = new BidSnapshotCacheEntry();
        entry.setSnapshot(dto);
        entry.setTopBidderId(topBidderId);
        bidSnapshotCache.put(auctionId, entry, ttlFor(auction));
        return entry;
    }

    // 마감까지 + 1시간. 마감이 없거나 지났으면 1시간
    private static Duration ttlFor(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        if (auction.getEndedAt() == null || !auction.getEndedAt().isAfter(now)) {
            return BidSnapshotCache.AFTER_END_TTL;
        }
        return Duration.between(now, auction.getEndedAt()).plus(BidSnapshotCache.AFTER_END_TTL);
    }

    // 다른 사람 id 는 뒤 두 자리만: "입찰자 **07"
    private static String mask(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return String.format("입찰자 **%02d", memberId % 100);
    }
}
