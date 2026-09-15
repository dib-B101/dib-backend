package com.b101.dib.member.query.service;

import com.b101.dib.member.query.dto.SettlementAccountDetailDto;

public interface SettlementAccountQueryService {
    SettlementAccountDetailDto findMine(Long memberId);
}
