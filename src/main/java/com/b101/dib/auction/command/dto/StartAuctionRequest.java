package com.b101.dib.auction.command.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 경매 시작 버튼을 누를 때 시작가·경매시간을 정한다. 이미 정해져 있으면 본문 없이 호출해도 된다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartAuctionRequest {
	private Long startPrice;
	private Integer auctionTime;
}
