package com.b101.dib.auction.query.service;

import com.b101.dib.auction.query.dto.AuctionQueryDto;
import com.b101.dib.auction.query.mapper.AuctionQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionQueryServiceImpl implements AuctionQueryService {
    private final AuctionQueryMapper auctionQueryMapper;
    public List<AuctionQueryDto> findAll() { return auctionQueryMapper.findAll(); }
    public List<AuctionQueryDto> findBySellerId(Long sellerId) { return auctionQueryMapper.findBySellerId(sellerId); }
    public List<AuctionQueryDto> findActive() { return auctionQueryMapper.findActive(); }
}
