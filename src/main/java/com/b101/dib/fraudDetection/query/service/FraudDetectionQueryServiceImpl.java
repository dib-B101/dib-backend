package com.b101.dib.fraudDetection.query.service;

import com.b101.dib.fraudDetection.query.dto.FraudDetectionQueryDto;
import com.b101.dib.fraudDetection.repository.FraudDetectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FraudDetectionQueryServiceImpl implements FraudDetectionQueryService {
    private final FraudDetectionMapper fraudDetectionMapper;

    @Override
    public List<FraudDetectionQueryDto> findAll(Long auctionId, Long memberId, Double riskScoreGte) {
        return fraudDetectionMapper.findAll(auctionId, memberId, riskScoreGte);
    }
}
