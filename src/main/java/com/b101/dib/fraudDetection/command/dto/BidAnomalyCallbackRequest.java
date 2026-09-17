package com.b101.dib.fraudDetection.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 명세 93 AI → 백엔드 콜백 본문
@Getter
@Setter
@NoArgsConstructor
public class BidAnomalyCallbackRequest {
    private String jobId;
    private Long auctionId;
    private Long memberId;
    private Long bidId;
    private BidAnomalyFeaturesDto features;
    private Integer predictedLabel;
    private Double decisionThreshold;
    private String modelVersion;
    private String featureVersion;
}
