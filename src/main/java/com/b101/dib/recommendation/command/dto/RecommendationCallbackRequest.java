package com.b101.dib.recommendation.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecommendationCallbackRequest {
    private String jobId;
    private Long memberId;
    private List<RecommendationItemDto> items = new ArrayList<>();
}
