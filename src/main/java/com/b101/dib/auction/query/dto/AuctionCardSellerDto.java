package com.b101.dib.auction.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트 ProductSellerSummaryDto
@Getter
@Setter
@NoArgsConstructor
public class AuctionCardSellerDto {
    private String nickname;
    // 받은 별점 평균(0~5). 후기가 없으면 null — 앱은 null 이면 평점 영역을 아예 안 그린다.
    // 예전엔 가입 때 박힌 50 이 그대로 내려갔다
    private Double rating;
    private Integer reviewCount;
    private Integer tradeCount;
    private Integer completedTradeCount;
}
