package com.b101.dib.bid.query.service;

import com.b101.dib.bid.query.dto.BidHistoryQueryDto;
import com.b101.dib.bid.query.dto.BidSnapshotDto;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.common.dto.CursorPageDto;

public interface BidQueryService {

    /** 특정 경매의 입찰 내역을 조회한다. */
    CursorPageDto<BidHistoryQueryDto> findByAuctionId(Long auctionId, String cursor, int size);

    /** 특정 회원의 입찰 내역을 조회한다. */
    CursorPageDto<MyBidQueryDto> findMine(
            Long memberId,
            AuctionStatus status,
            String cursor,
            int size
    );

    /** 특정 경매와 회원의 입찰 스냅샷을 조회한다. */
    BidSnapshotDto snapshot(Long auctionId, Long memberId);
}
