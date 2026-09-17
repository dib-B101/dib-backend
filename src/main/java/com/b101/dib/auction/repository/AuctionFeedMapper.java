package com.b101.dib.auction.repository;

import com.b101.dib.auction.query.dto.AuctionCardRowDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 홈·목록 카드 조회 (AuctionMapper 와 분리 — 카드는 조인이 많고 요청자별 컬럼이 있어서)
@Mapper
public interface AuctionFeedMapper {
    List<AuctionCardRowDto> findCards(@Param("memberId") Long memberId,
                                      @Param("scope") String scope,
                                      @Param("status") String status,
                                      @Param("categoryId") Long categoryId,
                                      @Param("minPrice") Long minPrice,
                                      @Param("maxPrice") Long maxPrice,
                                      @Param("sort") String sort,
                                      @Param("cursorId") Long cursorId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    List<AuctionCardRowDto> findBookmarkedCards(@Param("memberId") Long memberId,
                                                @Param("cursorId") Long cursorId,
                                                @Param("limit") int limit);

    AuctionCardRowDto findCardById(@Param("memberId") Long memberId, @Param("auctionId") Long auctionId);

    AuctionCardRowDto findActiveCardByLiveBroadcastId(@Param("memberId") Long memberId, @Param("liveBroadcastId") Long liveBroadcastId);
}
