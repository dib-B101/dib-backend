package com.b101.dib.auction.command.service;
import com.b101.dib.auction.command.dto.*;
import com.b101.dib.auction.domain.Auction;
public interface AuctionCommandService {
	
	Auction create(Long myId, CreateAuctionRequest request);
    
	Auction update(Long myId, Long auctionId, UpdateAuctionRequest request);
    
	Auction startAuction(Long myId, Long auctionId);

	Auction delete(Long myId, Long auctionId);

}
