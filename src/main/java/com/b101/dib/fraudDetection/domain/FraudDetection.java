package com.b101.dib.fraudDetection.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// AI 이상입찰 분석 결과 (명세 93 콜백을 그대로 저장). 판정만 저장하고 제재는 관리자 몫
@Entity
@Table(name = "fraud_detection")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long detectionId;

    private Long auctionId;
    private Long memberId;
    private Long bidId;
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
    private Short predictedLabel;
    private Double decisionThreshold;
    private String modelVersion;
    private String featureVersion;
    private LocalDateTime detectedAt;
}
