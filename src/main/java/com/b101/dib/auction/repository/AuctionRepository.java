package com.b101.dib.auction.repository;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.domain.AuctionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    // 입찰·종료는 경매 행을 잠그고 처리한다 (동시 입찰 직렬화)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.auctionId = :auctionId")
    Optional<Auction> findByIdForUpdate(@Param("auctionId") Long auctionId);

    List<Auction> findAllByStatusAndEndedAtBefore(AuctionStatus status, LocalDateTime before);
}
