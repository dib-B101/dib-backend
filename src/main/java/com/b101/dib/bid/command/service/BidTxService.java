package com.b101.dib.bid.command.service;

import com.b101.dib.bid.command.dto.BidPlacedDto;

// 입찰 트랜잭션 본체. 호출은 BidCommandServiceImpl(Redis 락) 을 통해서만
public interface BidTxService {
    BidPlacedDto place(Long auctionId, Long memberId, Long amount);
}
