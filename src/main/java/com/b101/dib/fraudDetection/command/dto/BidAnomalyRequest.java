package com.b101.dib.fraudDetection.command.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 명세 92 POST /internal/v1/ai/bid-anomalies 요청 — 입찰자 1명 단위
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidAnomalyRequest {
    private String jobId;
    private Long auctionId;
    private Long memberId;
    private Long bidId;
    private List<BidAnomalyBidDto> bids;
    private String callbackUrl;
}
