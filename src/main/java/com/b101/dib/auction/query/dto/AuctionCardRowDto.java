package com.b101.dib.auction.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 홈·목록 카드용 조회 행 (auction + product + category + seller + 내 북마크/입찰 조인). 평탄. AuctionCardDto.from 으로 프론트 계약 모양이 된다
@Getter
@Setter
@NoArgsConstructor
public class AuctionCardRowDto {
    private Long auctionId;
    private Long productId;
    private Long startPrice;
    private Long currentPrice;
    private Integer auctionTime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private AuctionStatus status;
    private Integer bidCount;
    private Integer bidderCount;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Long topBidId;
    private Long topBidderId;
    private Integer extensionCount;
    private Long liveBroadcastId;
    private LocalDateTime createdAt;
    // 찜 목록 조회에서만 채워진다 (bookmark_id 커서 페이징용)
    private Long bookmarkId;

    private Long sellerId;
    private String sellerNickname;
    private String sellerProfileImageUrl;
    private Double sellerScore;
    private Integer sellerReviewCount;
    private Integer sellerTradeCount;
    private Integer sellerCompletedTradeCount;

    private Long categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;

    private Boolean bookmarked;
    private Long myBidAmount;
    private Long myOrderId;
}
