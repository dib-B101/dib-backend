package com.b101.dib.fraudDetection.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 명세 93 콜백 features. AI 가 null 로 줄 수 있어 전부 nullable
@Getter
@Setter
@NoArgsConstructor
public class BidAnomalyFeaturesDto {
    private Double bidderTendency;
    private Double biddingRatio;
    private Double lastBidding;
    private Double auctionBids;
    private Double startingPriceAverage;
    private Double earlyBidding;
    private Double winningRatio;
    private Double auctionDuration;
    private Double ruleScore;
    private Double mlScore;
    private Double riskScore;
}
