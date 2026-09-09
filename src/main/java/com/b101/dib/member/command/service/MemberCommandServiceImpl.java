package com.b101.dib.member.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.SanctionMemberRequest;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberRepository memberRepository;

    @Override
    public Member sanction(Long memberId, SanctionMemberRequest request) {
        if (request.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (request.getWarningCount() != null) {
            member.setWarningCount(request.getWarningCount());
        }
        member.setStatus(request.getStatus());
        member.setSuspendedAt(request.getStatus() == MemberStatus.ACTIVE ? null : now);
        member.setUpdatedAt(now);
        return member;
    }

    @Override
    public Member releaseSanction(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.setStatus(MemberStatus.ACTIVE);
        member.setSuspendedAt(null);
        member.setUpdatedAt(LocalDateTime.now());
        return member;
    }
}
