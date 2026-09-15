package com.b101.dib.bid.command.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 입찰 성공 응답 (REST 201 / 소켓 BID_ACCEPTED 공용)
@Getter
@Setter
@NoArgsConstructor
public class BidPlacedDto {
    private Long bidId;
    private Long auctionId;
    private Long memberId;
    private Long amount;
    private Long currentPrice;
    private Long minAllowedAmount;
    private Integer bidCount;
    private Integer bidderCount;
    private String endedAt;
    private boolean extended;
    private Integer extensionCount;
    @JsonProperty("isHighestBidder")
    private boolean highestBidder;
    // OUTBID 알림 대상
    @JsonIgnore
    private Long previousTopBidderId;
}
