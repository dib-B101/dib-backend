package com.b101.dib.fraudDetection.command.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 명세 92 bids[] 한 건 (camelCase)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidAnomalyBidDto {
    private Long bidId;
    private Long memberId;
    private Long amount;
    private String createdAt;
}
