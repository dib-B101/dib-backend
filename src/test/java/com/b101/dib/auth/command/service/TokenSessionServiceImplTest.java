package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

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
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class TokenSessionServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-11T02:00:00Z");
    private static final Instant ABSOLUTE_EXPIRES_AT = NOW.plus(Duration.ofDays(60));

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private TokenIssuer tokenIssuer;
    @Mock
    private AccessTokenVerifier accessTokenVerifier;
    @Mock
    private RefreshSessionStore refreshSessionStore;

    private final RefreshTokenHasher refreshTokenHasher = new RefreshTokenHasher();
    private TokenSessionService service;

    @BeforeEach
    void setUp() {
        service = new TokenSessionServiceImpl(
                memberRepository,
                tokenIssuer,
                accessTokenVerifier,
                refreshTokenHasher,
                refreshSessionStore,
                jwtProperties(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void rotatesRefreshTokenAndKeepsFamilyAndAbsoluteExpiration() {
        Member member = activeMember();
        String presentedHash = refreshTokenHasher.hash("old-refresh-token");
        RefreshSession session = new RefreshSession(
                1L, "device-id", "family-id", ABSOLUTE_EXPIRES_AT
        );
        AuthTokenPair rotated = new AuthTokenPair(
                "new-access-token",
                "new-refresh-token",
                "new-refresh-hash",
                "family-id",
                NOW,
                ABSOLUTE_EXPIRES_AT
        );
        given(refreshSessionStore.findForRefresh(presentedHash, "device-id", NOW))
                .willReturn(session);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(tokenIssuer.rotate(member, "family-id", ABSOLUTE_EXPIRES_AT))
                .willReturn(rotated);

        TokenRefreshResponse response = service.refresh(
                new TokenRefreshRequest(" old-refresh-token ", " device-id ")
        );

        verify(refreshSessionStore).rotate(session, presentedHash, rotated);
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.accessExpiresIn()).isEqualTo(1800);
    }

    @Test
    void revokesSessionWhenMemberIsInactive() {
        Member member = activeMember();
        member.setStatus(MemberStatus.SUSPENDED);
        String presentedHash = refreshTokenHasher.hash("refresh-token");
        RefreshSession session = new RefreshSession(
                1L, "device-id", "family-id", ABSOLUTE_EXPIRES_AT
        );
        given(refreshSessionStore.findForRefresh(presentedHash, "device-id", NOW))
                .willReturn(session);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> service.refresh(
                new TokenRefreshRequest("refresh-token", "device-id")
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_REVOKED));

        verify(refreshSessionStore).revoke(1L, "device-id");
        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void returnsSessionRevokedWhenMemberDoesNotExist() {
        String presentedHash = refreshTokenHasher.hash("refresh-token");
        RefreshSession session = new RefreshSession(
                1L, "device-id", "family-id", ABSOLUTE_EXPIRES_AT
        );
        given(refreshSessionStore.findForRefresh(presentedHash, "device-id", NOW))
                .willReturn(session);
        given(memberRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(
                new TokenRefreshRequest("refresh-token", "device-id")
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.SESSION_REVOKED));

        verify(refreshSessionStore).revoke(1L, "device-id");
        verifyNoInteractions(tokenIssuer);
    }

    @Test
    void logsOutAuthenticatedMemberDevice() {
        given(accessTokenVerifier.verifyBearer("Bearer access-token"))
                .willReturn(new AccessTokenClaims(1L, MemberRole.USER));

        service.logout(
                "Bearer access-token",
                new LogoutRequest(" device-id ")
        );

        verify(refreshSessionStore).revoke(1L, "device-id");
    }

    @Test
    void doesNotTouchSessionWhenAccessTokenIsInvalid() {
        given(accessTokenVerifier.verifyBearer(null))
                .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED));

        assertThatThrownBy(() -> service.logout(null, new LogoutRequest("device-id")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.UNAUTHORIZED));

        verifyNoInteractions(refreshSessionStore);
    }

    private Member activeMember() {
        return Member.builder()
                .id(1L)
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .build();
    }

    private JwtProperties jwtProperties() {
        return new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
                1800,
                Duration.ofDays(30),
                Duration.ofDays(90)
        );
    }
}
