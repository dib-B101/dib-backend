package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.Instant;

import com.b101.dib.auth.command.dto.LogoutRequest;
import com.b101.dib.auth.command.dto.TokenRefreshRequest;
import com.b101.dib.auth.command.dto.TokenRefreshResponse;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.repository.RefreshSession;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.auth.token.RefreshTokenHasher;
import com.b101.dib.auth.token.TokenIssuer;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenSessionServiceImpl implements TokenSessionService {

    private final MemberRepository memberRepository;
    private final TokenIssuer tokenIssuer;
    private final AccessTokenVerifier accessTokenVerifier;
    private final RefreshTokenHasher refreshTokenHasher;
    private final RefreshSessionStore refreshSessionStore;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Override
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken().trim();
        String deviceId = request.deviceId().trim();
        Instant now = clock.instant();
        String presentedTokenHash = refreshTokenHasher.hash(refreshToken);

        RefreshSession session = refreshSessionStore.findForRefresh(
                presentedTokenHash,
                deviceId,
                now
        );
        Member member = memberRepository.findById(session.memberId()).orElse(null);
        if (member == null) {
            refreshSessionStore.revoke(session.memberId(), deviceId);
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }

        if (member.getStatus() != MemberStatus.ACTIVE || member.getDeletedAt() != null) {
            refreshSessionStore.revoke(member.getId(), deviceId);
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }

        AuthTokenPair tokens = tokenIssuer.rotate(
                member,
                session.familyId(),
                session.absoluteExpiresAt()
        );
        refreshSessionStore.rotate(session, presentedTokenHash, tokens);

        return new TokenRefreshResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                jwtProperties.accessTokenValiditySeconds()
        );
    }

    @Override
    public void logout(String authorizationHeader, LogoutRequest request) {
        AccessTokenClaims claims = accessTokenVerifier.verifyBearer(authorizationHeader);
        refreshSessionStore.revoke(claims.memberId(), request.deviceId().trim());
    }
}
