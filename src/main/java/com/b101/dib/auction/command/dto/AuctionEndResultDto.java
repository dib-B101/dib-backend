package com.b101.dib.auction.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 경매 종료 결과 (result = SOLD | UNSOLD). 소켓 AUCTION_ENDED 페이로드로도 그대로 나간다
@Getter
@Setter
@NoArgsConstructor
public class AuctionEndResultDto {
    private Long auctionId;
    private String result;
    private Long finalPrice;
    private Long winnerId;
    private Long orderId;
    private String endedAt;
}
