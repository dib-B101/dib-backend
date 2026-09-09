package com.b101.dib.fraudLabel.repository;

import com.b101.dib.fraudLabel.domain.FraudLabel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudLabelRepository extends JpaRepository<FraudLabel, Long> {
    boolean existsByAuctionIdAndMemberId(Long auctionId, Long memberId);
}
