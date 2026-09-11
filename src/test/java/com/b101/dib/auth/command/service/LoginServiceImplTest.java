package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.b101.dib.auth.command.dto.LoginRequest;
import com.b101.dib.auth.command.dto.LoginResponse;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.repository.RefreshSessionStore;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-10T01:00:00Z");

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenIssuer tokenIssuer;
    @Mock
    private RefreshSessionStore refreshSessionStore;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginServiceImpl(
                memberRepository,
                passwordEncoder,
                tokenIssuer,
                refreshSessionStore,
                jwtProperties(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void logsInActiveMemberAndIssuesTokens() {
        Member member = member(MemberStatus.ACTIVE);
        AuthTokenPair tokens = tokens();
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("Password1!", "encoded-password")).willReturn(true);
        given(tokenIssuer.issue(member)).willReturn(tokens);

        LoginResponse response = loginService.login(validRequest());

        assertThat(member.getLastLoginAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
        assertThat(member.getUpdatedAt()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
        verify(refreshSessionStore).save(1L, "device-id", tokens);
        assertThat(response.member().memberId()).isEqualTo(1L);
        assertThat(response.member().email()).isEqualTo("user@example.com");
        assertThat(response.member().nickname()).isEqualTo("길동이");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.accessExpiresIn()).isEqualTo(1800);
    }

    @Test
    void returnsInvalidCredentialsWhenEmailDoesNotExist() {
        given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> loginService.login(
                new LoginRequest(" unknown@example.com ", "wrong-password", "device-id")
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verify(passwordEncoder).matches(
                org.mockito.ArgumentMatchers.eq("wrong-password"),
                org.mockito.ArgumentMatchers.startsWith("$2a$10$")
        );
        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    @Test
    void returnsInvalidCredentialsWithRealBcryptWhenEmailDoesNotExist() {
        LoginService realPasswordService = new LoginServiceImpl(
                memberRepository,
                new BCryptPasswordEncoder(),
                tokenIssuer,
                refreshSessionStore,
                jwtProperties(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        given(memberRepository.findByEmail("unknown@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> realPasswordService.login(
                new LoginRequest("unknown@example.com", "wrong-password", "device-id")
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    @Test
    void returnsInvalidCredentialsWhenPasswordDoesNotMatch() {
        Member member = member(MemberStatus.ACTIVE);
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> loginService.login(
                new LoginRequest("user@example.com", "wrong-password", "device-id")
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    @Test
    void rejectsSuspendedMemberAfterCredentialVerification() {
        assertStatusError(MemberStatus.SUSPENDED, ErrorCode.ACCOUNT_SUSPENDED);
    }

    @Test
    void rejectsExpelledMemberAfterCredentialVerification() {
        assertStatusError(MemberStatus.EXPELLED, ErrorCode.ACCOUNT_BLOCKED);
    }

    @Test
    void treatsWithdrawnMemberAsInvalidCredentials() {
        assertStatusError(MemberStatus.WITHDRAWN, ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void treatsDeletedActiveMemberAsInvalidCredentials() {
        Member member = member(MemberStatus.ACTIVE);
        member.setDeletedAt(LocalDateTime.ofInstant(NOW.minusSeconds(60), ZoneOffset.UTC));
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("Password1!", "encoded-password")).willReturn(true);

        assertThatThrownBy(() -> loginService.login(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        assertThat(member.getLastLoginAt()).isNull();
        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    @Test
    void treatsMemberWithoutPasswordAsInvalidCredentials() {
        Member member = member(MemberStatus.ACTIVE);
        member.setPassword(null);
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));

        assertThatThrownBy(() -> loginService.login(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verify(passwordEncoder).matches(
                org.mockito.ArgumentMatchers.eq("Password1!"),
                org.mockito.ArgumentMatchers.startsWith("$2a$10$")
        );
        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    private void assertStatusError(MemberStatus status, ErrorCode expectedErrorCode) {
        Member member = member(status);
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(passwordEncoder.matches("Password1!", "encoded-password")).willReturn(true);

        assertThatThrownBy(() -> loginService.login(validRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(expectedErrorCode));

        assertThat(member.getLastLoginAt()).isNull();
        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    private LoginRequest validRequest() {
        return new LoginRequest(" User@Example.com ", "Password1!", " device-id ");
    }

    private Member member(MemberStatus status) {
        return Member.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .nickname("길동이")
                .status(status)
                .role(MemberRole.USER)
                .build();
    }

    private AuthTokenPair tokens() {
        return new AuthTokenPair(
                "access-token",
                "refresh-token",
                "refresh-token-hash",
                "family-id",
                NOW,
                NOW.plus(Duration.ofDays(90))
        );
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
