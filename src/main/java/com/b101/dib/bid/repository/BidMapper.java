package com.b101.dib.bid.repository;

import com.b101.dib.bid.query.dto.BidHistoryQueryDto;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BidMapper {
    List<BidHistoryQueryDto> findByAuctionId(@Param("auctionId") Long auctionId,
                                             @Param("cursor") Long cursor,
                                             @Param("limit") int limit);
    List<MyBidQueryDto> findByMemberId(@Param("memberId") Long memberId,
                                       @Param("cursor") Long cursor,
                                       @Param("limit") int limit);
}
