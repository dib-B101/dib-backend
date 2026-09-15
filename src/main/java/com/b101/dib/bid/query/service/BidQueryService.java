package com.b101.dib.bid.query.service;

import com.b101.dib.bid.query.dto.BidHistoryQueryDto;
import com.b101.dib.bid.query.dto.BidSnapshotDto;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.common.dto.CursorPageDto;

public interface BidQueryService {
    CursorPageDto<BidHistoryQueryDto> findByAuctionId(Long auctionId, String cursor, int size);
    CursorPageDto<MyBidQueryDto> findMine(Long memberId, String cursor, int size);
    BidSnapshotDto snapshot(Long auctionId, Long memberId);
}
