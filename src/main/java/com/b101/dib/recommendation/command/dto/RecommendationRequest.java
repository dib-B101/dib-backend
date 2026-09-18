package com.b101.dib.recommendation.command.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RecommendationRequest {
    private String jobId;
    private Long memberId;
    private String scope;
    private Object behaviorWindow;
    @Builder.Default
    private List<Long> candidateAuctionIds = List.of();
    private String callbackUrl;
}
