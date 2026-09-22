package com.b101.dib.product.query.dto;

import java.time.LocalDateTime;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.domain.ProductStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDto {
	private Long productId;
    private Long memberId;
    private Long categoryId;
    private String title;
    private String description;
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;
    private ProductStatus productStatus;
//    private String embedding;
    private LocalDateTime productCreatedAt;
    private LocalDateTime productUpdatedAt;
    private LocalDateTime productDeletedAt;
    // 검수 단계. PENDING 일 때 moderatedAt 이 null 이면 AI 검수 중, 있으면 관리자 검토 대기 (stage: rule | ai | fallback)
    private String moderationStage;
    private LocalDateTime moderatedAt;
	
    private Long auctionId;
//    private Long productId;
    private Long startPrice;
    private Long currentPrice;
    private Integer auctionTime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private AuctionStatus auctionStatus;
    private Integer bidCount;
    private Integer bidderCount;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Long topBidId;
    private Integer extensionCount;
    private LocalDateTime auctionCreatedAt;
    private LocalDateTime auctionUpdatedAt;
    private LocalDateTime auctionDeletedAt;
    private Long liveBroadcastId;
    
}
