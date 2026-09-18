package com.b101.dib.recommendation.query.dto;

import com.b101.dib.recommendation.command.dto.RecommendationItemDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecommendationSnapshot {
    private String jobId;
    private Long memberId;
    private String cachedAt;
    private List<RecommendationItemDto> items = new ArrayList<>();
}
