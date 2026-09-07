package com.b101.dib.auction.query.service;

import com.b101.dib.auction.query.dto.AuctionQueryDto;
import java.util.List;

public interface AuctionQueryService {
    List<AuctionQueryDto> findAll();
    List<AuctionQueryDto> findBySellerId(Long sellerId);
    List<AuctionQueryDto> findActive();
}
