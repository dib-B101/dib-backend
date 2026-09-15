package com.b101.dib.bid.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MyBidQueryDto {
    private Long bidId;
    private Long auctionId;
    private Long productId;
    private String productTitle;
    private String thumbnailUrl;
    private Long amount;
    private Long currentPrice;
    private AuctionStatus auctionStatus;
    @JsonProperty("isHighestBidder")
    private boolean highest;
    private LocalDateTime createdAt;
}
