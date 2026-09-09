package com.b101.dib.auction.query.dto;

import lombok.*;
import java.time.LocalDateTime;

import com.b101.dib.auction.domain.AuctionStatus;

@Getter @Setter @Builder
public class AuctionQueryDto {
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
    private Integer extensionCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long liveBroadcastId;
}
