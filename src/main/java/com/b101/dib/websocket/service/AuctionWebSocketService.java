package com.b101.dib.websocket.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuctionWebSocketService {
	
    private final AuctionRedisService auctionRedisService;
    private final AuctionWebSocketService auctionWebSocketService;

    public void bid(Long auctionId, Long memberId, Long price) {

        AuctionRedisData data = auctionRedisService.bid(auctionId, memberId, price);

        auctionWebSocketService.broadcast(auctionId, data);
    }

}
