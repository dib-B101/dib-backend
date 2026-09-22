package com.b101.dib.auction.query.service;

import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.query.dto.AuctionRecommendationDto;
import com.b101.dib.common.dto.CursorPageDto;

// 홈·목록 카드 조회 (프론트 AuctionDto / AuctionRecommendationResponse 계약)
public interface AuctionFeedQueryService {
    // mine=true 면 memberId 가 판매자인 경매만 (라이브 편성 후보). 로그인 정보가 없으면 무시된다
    CursorPageDto<AuctionCardDto> findCards(Long memberId, boolean mine, String scope, String status, Long categoryId,
                                            Long minPrice, Long maxPrice, String sort, String cursor, int size);

    // 내가 찜한 상품의 경매 카드 (찜 목록 화면)
    CursorPageDto<AuctionCardDto> findBookmarkedCards(Long memberId, String cursor, int size);

    AuctionRecommendationDto recommend(Long memberId, int size);

    AuctionCardDto findCard(Long memberId, Long auctionId);
}
