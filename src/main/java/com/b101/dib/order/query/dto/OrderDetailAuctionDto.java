package com.b101.dib.order.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailAuctionDto {
    private Long auctionId;
    private Long productId;
    private String title;
    private Long startPrice;
    private Long currentPrice;
    private AuctionStatus status;
    private LocalDateTime endedAt;
}
