package com.b101.dib.recommendation.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RecommendationItemDto {
    private Long auctionId;
    private Double score;
    private String reason;
}
