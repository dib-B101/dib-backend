package com.b101.dib.member.command.service;

import com.b101.dib.auth.command.service.PhoneVerificationService;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
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
    private final PhoneVerificationService phoneVerificationService;

    @Override
    public Member update(Long memberId, UpdateSettlementAccountRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        // 계좌 변경은 민감 정보 변경 — auth 의 휴대폰 재인증 토큰을 소비한다 (기능 명세 "변경 시 휴대전화 재인증")
        phoneVerificationService.consumeVerificationToken(
                request.getPhoneVerificationToken(), PhoneVerificationPurpose.CHANGE_SENSITIVE, member.getPhoneNumber());
        member.setBankName(request.getBankName().trim());
        member.setAccountNumber(request.getAccountNumber().replace("-", "").trim());
        member.setAccountHolder(request.getAccountHolder().trim());
        member.setUpdatedAt(LocalDateTime.now());
        return member;
    }
}
