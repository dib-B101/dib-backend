package com.b101.dib.fraudDetection.repository;

import com.b101.dib.fraudDetection.domain.FraudDetection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudDetectionRepository extends JpaRepository<FraudDetection, Long> {
    boolean existsByAuctionIdAndMemberIdAndFeatureVersion(Long auctionId, Long memberId, String featureVersion);
}
