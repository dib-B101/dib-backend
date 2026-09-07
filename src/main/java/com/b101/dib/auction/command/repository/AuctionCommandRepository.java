package com.b101.dib.auction.command.repository;

import com.b101.dib.auction.command.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionCommandRepository extends JpaRepository<Auction, Long> {
}
