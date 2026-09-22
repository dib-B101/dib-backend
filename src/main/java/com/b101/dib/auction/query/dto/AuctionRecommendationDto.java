package com.b101.dib.auction.query.dto;

import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastCardDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// 프론트 AuctionRecommendationResponse: 홈 상단 라이브 + 일반 추천 카드
@Getter
@Setter
@NoArgsConstructor
public class AuctionRecommendationDto {
    private List<LiveBroadcastCardDto> liveItems = new ArrayList<>();
    private List<AuctionCardDto> generalItems = new ArrayList<>();
    private String nextCursor;
    private boolean hasNext;
    private String serverTime;
}
