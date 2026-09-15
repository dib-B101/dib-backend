package com.b101.dib.auction.command.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;

public interface AuctionEndTxService {
    // 아직 ACTIVE 고 마감이 지났으면 종료 처리. 이미 처리됐으면 null
    AuctionEndResultDto endOne(Long auctionId);
}
