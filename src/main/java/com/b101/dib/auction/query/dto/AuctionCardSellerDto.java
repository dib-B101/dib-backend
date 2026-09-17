package com.b101.dib.auction.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트 ProductSellerSummaryDto
@Getter
@Setter
@NoArgsConstructor
public class AuctionCardSellerDto {
    private String nickname;
    private Double rating;
    private Integer tradeCount;
    private Integer completedTradeCount;
}
