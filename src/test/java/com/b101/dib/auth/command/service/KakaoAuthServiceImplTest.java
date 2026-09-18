package com.b101.dib.auth.command.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;
import com.b101.dib.auth.command.dto.KakaoSignupRequest;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.config.KakaoProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.external.kakao.KakaoOAuthClient;
import com.b101.dib.auth.external.kakao.KakaoProfile;
import com.b101.dib.auth.repository.KakaoSignupSession;
import com.b101.dib.auth.repository.KakaoSignupTokenStore;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.KakaoSignupTokenHasher;
import com.b101.dib.auth.token.TokenIssuer;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.domain.SocialAccount;
import com.b101.dib.member.domain.SocialProvider;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.member.repository.SocialAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-18T01:00:00Z");
    private static final Duration SIGNUP_TTL = Duration.ofMinutes(10);

    @Mock private KakaoOAuthClient kakaoOAuthClient;
    @Mock private KakaoSignupTokenStore kakaoSignupTokenStore;
    @Mock private SocialAccountRepository socialAccountRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private PhoneVerificationService phoneVerificationService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenIssuer tokenIssuer;
    @Mock private RefreshSessionStore refreshSessionStore;

    private KakaoAuthService service;

    @BeforeEach
    void setUp() {
        service = new KakaoAuthServiceImpl(
                kakaoOAuthClient,
                kakaoSignupTokenStore,
                new KakaoSignupTokenHasher(),
                socialAccountRepository,
                memberRepository,
                phoneVerificationService,
                passwordEncoder,
                tokenIssuer,
                refreshSessionStore,
                new JwtProperties("secret", 1800, Duration.ofDays(30), Duration.ofDays(90)),
                new KakaoProperties(
                        "client-id", "client-secret", "token-url", "user-info-url", SIGNUP_TTL
                ),
                new SecureRandom(),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void logsInMemberAlreadyLinkedToKakao() {
        stubKakaoProfile();
        Member member = member(1L, "user@example.com", "01012345678");
        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .provider(SocialProvider.KAKAO)
                .providerUserId("12345")
                .createdAt(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC))
                .build();
        given(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.KAKAO, "12345"
        )).willReturn(Optional.of(socialAccount));
        given(tokenIssuer.issue(member)).willReturn(tokens());

        KakaoAuthResponse response = service.authenticate(authRequest());

        assertThat(response.isNewMember()).isFalse();
        assertThat(response.member().memberId()).isEqualTo(1L);
        assertThat(response.signupToken()).isNull();
        verify(refreshSessionStore).save(1L, "device-id", tokens());
        verifyNoInteractions(kakaoSignupTokenStore, phoneVerificationService);
    }

    @Test
    void returnsOneTimeSignupTokenForUnlinkedKakaoUser() {
        stubKakaoProfile();
        given(socialAccountRepository.findByProviderAndProviderUserId(
                SocialProvider.KAKAO, "12345"
        )).willReturn(Optional.empty());

        KakaoAuthResponse response = service.authenticate(authRequest());

        assertThat(response.isNewMember()).isTrue();
        assertThat(response.member()).isNull();
        assertThat(response.signupToken()).isNotBlank();
        assertThat(response.kakaoProfile().nickname()).isEqualTo("카카오닉네임");
        assertThat(response.accessToken()).isNull();
        ArgumentCaptor<KakaoSignupSession> sessionCaptor =
                ArgumentCaptor.forClass(KakaoSignupSession.class);
        verify(kakaoSignupTokenStore).save(anyString(), sessionCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(SIGNUP_TTL));
        assertThat(sessionCaptor.getValue().providerUserId()).isEqualTo("12345");
        verifyNoInteractions(memberRepository, phoneVerificationService, tokenIssuer);
    }

    @Test
    void createsAndLinksNewMemberWithDibSignupInformation() {
        given(kakaoSignupTokenStore.find(anyString())).willReturn(signupSession());
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.empty());
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.empty());
        given(memberRepository.existsByNickname("길동이")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encoded-random-password");
        given(memberRepository.saveAndFlush(any())).willAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        given(tokenIssuer.issue(any())).willReturn(tokens());

        KakaoAuthResponse response = service.signup(signupRequest());

        assertThat(response.isNewMember()).isTrue();
        assertThat(response.member().memberId()).isEqualTo(10L);
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getEmail()).isEqualTo("user@example.com");
        assertThat(memberCaptor.getValue().getProfileImageUrl())
                .isEqualTo("https://image.example/profile.jpg");
        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token", PhoneVerificationPurpose.SIGN_UP, "01012345678"
        );
        verify(socialAccountRepository).save(any(SocialAccount.class));
    }

    @Test
    void linksExistingMemberWithSameVerifiedPhone() {
        Member member = member(1L, "user@example.com", "01012345678");
        given(kakaoSignupTokenStore.find(anyString())).willReturn(signupSession());
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.of(member));
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(tokenIssuer.issue(member)).willReturn(tokens());

        KakaoAuthResponse response = service.signup(signupRequest());

        assertThat(response.isNewMember()).isFalse();
        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token", PhoneVerificationPurpose.SIGN_UP, "01012345678"
        );
        verify(socialAccountRepository).save(any(SocialAccount.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void requiresAccountLinkWhenEmailBelongsToDifferentIdentity() {
        given(kakaoSignupTokenStore.find(anyString())).willReturn(signupSession());
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.empty());
        given(memberRepository.findByEmail("user@example.com"))
                .willReturn(Optional.of(member(1L, "user@example.com", "01099998888")));

        assertThatThrownBy(() -> service.signup(signupRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ACCOUNT_LINK_REQUIRED));

        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    @Test
    void rejectsExpiredOrReusedSignupToken() {
        given(kakaoSignupTokenStore.find(anyString()))
                .willThrow(new BusinessException(ErrorCode.INVALID_KAKAO_SIGNUP_TOKEN));

        assertThatThrownBy(() -> service.signup(signupRequest()))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_KAKAO_SIGNUP_TOKEN));

        verifyNoInteractions(phoneVerificationService, memberRepository, tokenIssuer);
    }

    private void stubKakaoProfile() {
        given(kakaoOAuthClient.authenticate("authorization-code", "http://localhost/callback"))
                .willReturn(new KakaoProfile(
                        "12345",
                        "카카오닉네임",
                        "https://image.example/profile.jpg"
                ));
    }

    private KakaoAuthRequest authRequest() {
        return new KakaoAuthRequest(
                " authorization-code ",
                " http://localhost/callback ",
                " device-id "
        );
    }

    private KakaoSignupRequest signupRequest() {
        return new KakaoSignupRequest(
                "signup-token",
                " User@Example.com ",
                " 홍길동 ",
                " 길동이 ",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                "010-1234-5678",
                " verification-token ",
                " device-id "
        );
    }

    private KakaoSignupSession signupSession() {
        return new KakaoSignupSession(
                "12345",
                "카카오닉네임",
                "https://image.example/profile.jpg"
        );
    }

    private Member member(Long id, String email, String phoneNumber) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname("길동이")
                .phoneNumber(phoneNumber)
                .status(MemberStatus.ACTIVE)
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
}
