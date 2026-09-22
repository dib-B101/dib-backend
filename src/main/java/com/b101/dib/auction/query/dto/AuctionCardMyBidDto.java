package com.b101.dib.auction.query.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트 MyBidDto
@Getter
@Setter
@NoArgsConstructor
public class AuctionCardMyBidDto {
    private Long amount;
    @JsonProperty("isHighestBidder")
    private boolean highestBidder;
}
