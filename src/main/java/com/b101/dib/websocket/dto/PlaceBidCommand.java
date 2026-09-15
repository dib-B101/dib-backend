package com.b101.dib.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 클라이언트 → /app/auctions/{auctionId}/bids
@Getter
@Setter
@NoArgsConstructor
public class PlaceBidCommand {
    private String commandId;
    private Long amount;
}
