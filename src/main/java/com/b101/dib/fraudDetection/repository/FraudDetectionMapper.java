package com.b101.dib.fraudDetection.repository;

import com.b101.dib.fraudDetection.query.dto.FraudDetectionQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FraudDetectionMapper {
    List<FraudDetectionQueryDto> findAll(@Param("auctionId") Long auctionId,
                                         @Param("memberId") Long memberId,
                                         @Param("riskScoreGte") Double riskScoreGte);
}
