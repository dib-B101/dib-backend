package com.b101.dib.member.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.UpdateSettlementAccountRequest;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementAccountCommandServiceImpl implements SettlementAccountCommandService {
    private final MemberRepository memberRepository;

    @Override
    public Member update(Long memberId, UpdateSettlementAccountRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        // 가입 단계에서 이미 휴대폰 본인인증을 거치므로 정산 계좌 등록·변경에서는 재인증을 받지 않는다
        member.setBankName(request.getBankName().trim());
        member.setAccountNumber(request.getAccountNumber().replace("-", "").trim());
        member.setAccountHolder(request.getAccountHolder().trim());
        member.setUpdatedAt(LocalDateTime.now());
        return member;
    }
}
