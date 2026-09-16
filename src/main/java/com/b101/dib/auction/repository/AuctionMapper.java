package com.b101.dib.auction.repository;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.AuctionQueryDto;
import com.b101.dib.auction.query.dto.SaleHistoryRowDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuctionMapper {
    List<AuctionQueryDto> findAll();
    List<AuctionQueryDto> findBySellerId(@Param("sellerId") Long sellerId);
    List<AuctionQueryDto> findActive();
	AuctionQueryDto findById(Long auctionId);
	List<AuctionQueryDto> findRecommendations();
    List<SaleHistoryRowDto> findSalesByMemberId(
            @Param("memberId") Long memberId,
            @Param("auctionStatus") AuctionStatus auctionStatus,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );
}
