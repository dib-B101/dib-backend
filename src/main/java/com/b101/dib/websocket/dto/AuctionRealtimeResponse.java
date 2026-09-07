package com.b101.dib.websocket.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuctionRealtimeResponse {
	Long auctionId;
	Long currentPrice;
	Long topBidderId;
	Integer bitCount;
	Integer extensionCount;
	LocalDateTime endedAt;
}
