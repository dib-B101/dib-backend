package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import com.b101.dib.auth.command.dto.SignupRequest;
import com.b101.dib.auth.command.dto.SignupResponse;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.domain.PasswordPolicy;
import com.b101.dib.auth.domain.PhoneNumber;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.TokenIssuer;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupServiceImpl implements SignupService {

    private final MemberRepository memberRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshSessionStore refreshSessionStore;
    private final Clock clock;

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String nickname = request.nickname().trim();
        String name = request.name().trim();
        String phoneNumber = PhoneNumber.from(request.phoneNumber()).value();
        String deviceId = request.deviceId().trim();

        validatePassword(request.password());
        validateDuplicates(email, nickname, phoneNumber);

        phoneVerificationService.consumeVerificationToken(
                request.phoneVerificationToken(),
                PhoneVerificationPurpose.SIGN_UP,
                phoneNumber
        );

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .nickname(nickname)
                .name(name)
                .gender(request.gender())
                .birthDate(request.birthDate())
                .phoneNumber(phoneNumber)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .score(50.0)
                .createdAt(now)
                .updatedAt(now)
                .warningCount(0)
                .build();

        Member savedMember = saveMember(member);
        AuthTokenPair tokens = tokenIssuer.issue(savedMember);
        refreshSessionStore.save(savedMember.getId(), deviceId, tokens);

        return new SignupResponse(
                savedMember.getId(),
                savedMember.getEmail(),
                savedMember.getNickname(),
                savedMember.getStatus(),
                savedMember.getRole(),
                tokens.accessToken(),
                tokens.refreshToken()
        );
    }

    private void validatePassword(String password) {
        if (!PasswordPolicy.isValid(password)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    private void validateDuplicates(String email, String nickname, String phoneNumber) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATED);
        }
    }

    private Member saveMember(Member member) {
        try {
            return memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            String cause = rootCauseMessage(e).toLowerCase(Locale.ROOT);
            if (cause.contains("email")) {
                throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
            }
            if (cause.contains("nickname")) {
                throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
            }
            if (cause.contains("phone")) {
                throw new BusinessException(ErrorCode.PHONE_DUPLICATED);
            }
            throw e;
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
