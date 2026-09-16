package com.b101.dib.bid.query.service;

import com.b101.dib.bid.query.dto.BidHistoryQueryDto;
import com.b101.dib.bid.query.dto.BidSnapshotDto;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.common.dto.CursorPageDto;

public interface BidQueryService {
    CursorPageDto<BidHistoryQueryDto> findByAuctionId(Long auctionId, String cursor, int size);
    CursorPageDto<MyBidQueryDto> findMine(Long memberId, String cursor, int size);
    BidSnapshotDto snapshot(Long auctionId, Long memberId);

    // DB 에서 다시 읽어 Redis Hot State 를 덮어쓴다 (입찰·종료 커밋 후)
    void refreshSnapshot(Long auctionId);
}
