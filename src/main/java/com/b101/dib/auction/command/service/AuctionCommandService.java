package com.b101.dib.auction.command.service;
import com.b101.dib.auction.command.dto.*;
public interface AuctionCommandService {
	
    Long create(Long myId, CreateAuctionRequest request);
    
    void update(Long myId, Long auctionId, UpdateAuctionRequest request);
    
    void delete(Long myId, Long auctionId);
}
