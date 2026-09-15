package com.b101.dib.bid.command.service;

import com.b101.dib.bid.command.dto.BidPlacedDto;

public interface BidCommandService {
    BidPlacedDto place(Long auctionId, Long memberId, Long amount);
}
