package com.b101.dib.bid.repository;

import com.b101.dib.bid.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
    boolean existsByAuctionIdAndMemberId(Long auctionId, Long memberId);
}
