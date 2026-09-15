package com.b101.dib.settlement.query.service;

import com.b101.dib.settlement.query.dto.SettlementDetailDto;
import com.b101.dib.settlement.query.dto.SettlementQueryDto;

import java.util.List;

public interface SettlementQueryService {
    List<SettlementQueryDto> findMine(Long sellerId);
    SettlementDetailDto findDetail(Long memberId, Long settlementId);
}
