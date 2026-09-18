package com.b101.dib.liveBroadcast.command.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 라이브 편성 시점에 경매별 시작가·경매시간을 함께 정한다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveItemRequest {
	private Long auctionId;
	private Long startPrice;
	private Integer auctionTime;
}
