package com.b101.dib.bid.query.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.bid.domain.Bid;
import com.b101.dib.bid.query.dto.BidHistoryQueryDto;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BidQueryServiceImpl implements BidQueryService {
    private final BidMapper bidMapper;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;

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

    @Override
    public BidSnapshotDto snapshot(Long auctionId, Long memberId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        boolean highest = false;
        if (memberId != null && auction.getTopBidId() != null) {
            Bid top = bidRepository.findById(auction.getTopBidId()).orElse(null);
            highest = top != null && top.getMemberId().equals(memberId);
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
        dto.setHighestBidder(highest);
        dto.setServerTime(Times.now());
        return dto;
    }

    // 다른 사람 id 는 뒤 두 자리만: "입찰자 **07"
    private static String mask(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return String.format("입찰자 **%02d", memberId % 100);
    }
}
