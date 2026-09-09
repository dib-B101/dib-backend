package com.b101.dib.fraudDetection.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class FraudDetectionQueryDto {
    private Long detectionId;
    private Long auctionId;
    private Long memberId;
    private String memberNickname;
    private Long bidId;
    private Double ruleScore;
    private Double mlScore;
    private Double riskScore;
    private Short predictedLabel;
    private Double decisionThreshold;
    private String modelVersion;
    private String featureVersion;
    private LocalDateTime detectedAt;
}
