package com.b101.dib.member.command.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.SanctionMemberRequest;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandServiceImpl implements MemberCommandService {
    private final MemberRepository memberRepository;
    private final Clock clock;

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

    @Override
    public UpdateProfileResponse updateProfile(Long memberId, UpdateProfileRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (request.nickname() != null) {
            String nickname = request.nickname().trim();
            if (!nickname.equals(member.getNickname()) && memberRepository.existsByNickname(nickname)) {
                throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
            }
            member.setNickname(nickname);
        }

        if (request.profileImageUrl() != null) {
            String profileImageUrl = request.profileImageUrl().trim();
            member.setProfileImageUrl(profileImageUrl.isEmpty() ? null : profileImageUrl);
        }

        LocalDateTime updatedAt = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        member.setUpdatedAt(updatedAt);
        saveMember(member);

        return new UpdateProfileResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl(),
                updatedAt
        );
    }

    private void saveMember(Member member) {
        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException exception) {
            if (rootCauseMessage(exception).toLowerCase(Locale.ROOT).contains("nickname")) {
                throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
            }
            throw exception;
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? "" : cause.getMessage();
    }
}
