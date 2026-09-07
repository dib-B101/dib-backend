package com.b101.dib.auction.command.service;
import com.b101.dib.auction.command.dto.*;
public interface AuctionCommandService {
    Long create(Long memberId, CreateAuctionRequest request);
    void update(Long memberId, Long auctionId, UpdateAuctionRequest request);
    void delete(Long memberId, Long auctionId);
}
