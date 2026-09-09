package com.b101.dib.fraudDetection.query.service;

import com.b101.dib.fraudDetection.query.dto.FraudDetectionQueryDto;

import java.util.List;

public interface FraudDetectionQueryService {
    List<FraudDetectionQueryDto> findAll(Long auctionId, Long memberId, Double riskScoreGte);
}
