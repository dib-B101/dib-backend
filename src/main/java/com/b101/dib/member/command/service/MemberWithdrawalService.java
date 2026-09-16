package com.b101.dib.member.command.service;

import com.b101.dib.member.command.dto.WithdrawalRequest;
import com.b101.dib.member.command.dto.WithdrawalResponse;

public interface MemberWithdrawalService {

    WithdrawalResponse request(Long memberId, WithdrawalRequest request);

    int completeDueWithdrawals();
}
