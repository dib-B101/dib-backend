package com.b101.dib.auction.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.auction.domain.Auction;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
