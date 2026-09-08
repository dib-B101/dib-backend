package com.b101.dib.auction.query.dto;

import lombok.*;
import java.time.LocalDateTime;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.domain.AuctionType;

@Getter @Setter @Builder
public class AuctionQueryDto {
    private Long auctionId;
    private Long memberId;
    private Long productId;
    private Long categoryId;
    private Long startPrice;
    private Long currentPrice;
    private Long bidIncrement;
    private Integer auctionTime;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private AuctionStatus status;
    private AuctionType auctionType;
    private Integer bidCount;
    private Integer bidderCount;
    private Integer viewCount;
    private Integer bookmarkCount;
    private Long topBidId;
    private Integer extensionCount;
    private Long depositAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long liveBroadcastId;
    private Long version;
}
