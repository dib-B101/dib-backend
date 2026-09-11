package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

import com.b101.dib.auth.command.dto.LoginMemberResponse;
import com.b101.dib.auth.command.dto.LoginRequest;
import com.b101.dib.auth.command.dto.LoginResponse;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.TokenIssuer;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshSessionStore refreshSessionStore;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String deviceId = request.deviceId().trim();
        String rawPassword = request.password() == null ? "" : request.password();

        Member member = authenticate(email, rawPassword);
        validateAccountStatus(member);

        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        member.setLastLoginAt(now);
        member.setUpdatedAt(now);

        AuthTokenPair tokens = tokenIssuer.issue(member);
        refreshSessionStore.save(member.getId(), deviceId, tokens);

        return new LoginResponse(
                new LoginMemberResponse(
                        member.getId(),
                        member.getEmail(),
                        member.getNickname(),
                        member.getStatus(),
                        member.getRole()
                ),
                tokens.accessToken(),
                tokens.refreshToken(),
                jwtProperties.accessTokenValiditySeconds()
        );
    }

    private Member authenticate(String email, String rawPassword) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        String encodedPassword = member == null || member.getPassword() == null
                ? DUMMY_PASSWORD_HASH
                : member.getPassword();
        boolean passwordMatches = passwordEncoder.matches(rawPassword, encodedPassword);

        if (member == null || member.getPassword() == null || !passwordMatches) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return member;
    }

    private void validateAccountStatus(Member member) {
        MemberStatus status = member.getStatus();
        if (status == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (status == MemberStatus.EXPELLED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BLOCKED);
        }
        if (status != MemberStatus.ACTIVE || member.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
