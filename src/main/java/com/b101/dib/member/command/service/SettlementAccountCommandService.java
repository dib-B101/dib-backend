package com.b101.dib.member.command.service;

import com.b101.dib.member.command.dto.UpdateSettlementAccountRequest;
import com.b101.dib.member.domain.Member;

public interface SettlementAccountCommandService {
    Member update(Long memberId, UpdateSettlementAccountRequest request);
}
