package com.b101.dib.auction.repository;

import com.b101.dib.auction.query.dto.AuctionQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AuctionMapper {
    List<AuctionQueryDto> findAll();
    List<AuctionQueryDto> findBySellerId(@Param("sellerId") Long sellerId);
    List<AuctionQueryDto> findActive();
}
