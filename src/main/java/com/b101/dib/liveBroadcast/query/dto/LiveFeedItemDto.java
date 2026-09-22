package com.b101.dib.liveBroadcast.query.dto;

import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.query.dto.AuctionCardProductDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트 LiveFeedItemDto: 방송 + 지금 진행 중(또는 다음) 경매 + 그 상품
@Getter
@Setter
@NoArgsConstructor
public class LiveFeedItemDto {
    private LiveBroadcastCardDto liveBroadcast;
    private AuctionCardDto activeAuction;
    private AuctionCardProductDto product;
}
