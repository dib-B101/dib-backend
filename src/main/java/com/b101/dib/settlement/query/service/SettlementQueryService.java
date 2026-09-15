package com.b101.dib.settlement.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.settlement.query.dto.SettlementDetailDto;
import com.b101.dib.settlement.query.dto.SettlementQueryDto;


public interface SettlementQueryService {
    CursorPageDto<SettlementQueryDto> findMine(Long sellerId, String cursor, int size);
    SettlementDetailDto findDetail(Long memberId, Long settlementId);
}
