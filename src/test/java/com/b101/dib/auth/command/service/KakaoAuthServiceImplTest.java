package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.external.kakao.KakaoOAuthClient;
import com.b101.dib.auth.external.kakao.KakaoProfile;
import com.b101.dib.auth.repository.RefreshSessionStore;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KakaoAuthServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-18T01:00:00Z");

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;
    @Mock
    private SocialAccountRepository socialAccountRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PhoneVerificationService phoneVerificationService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenIssuer tokenIssuer;
    @Mock
    private RefreshSessionStore refreshSessionStore;

    private KakaoAuthService service;

    @BeforeEach
    void setUp() {
        service = new KakaoAuthServiceImpl(
                kakaoOAuthClient,
                socialAccountRepository,
                memberRepository,
                phoneVerificationService,
                passwordEncoder,
                tokenIssuer,
                refreshSessionStore,
                new JwtProperties("secret", 1800, Duration.ofDays(30), Duration.ofDays(90)),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
        given(kakaoOAuthClient.authenticate("authorization-code", "http://localhost/callback"))
                .willReturn(profile());
    }

    @Test
    void logsInMemberAlreadyLinkedToKakao() {
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

        KakaoAuthResponse response = service.authenticate(request(null));

        assertThat(response.isNewMember()).isFalse();
        assertThat(response.member().memberId()).isEqualTo(1L);
        verify(refreshSessionStore).save(1L, "device-id", tokens());
        verifyNoInteractions(phoneVerificationService);
    }

    @Test
    void createsAndLinksNewMemberAfterPhoneVerification() {
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.empty());
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.empty());
        given(memberRepository.existsByNickname("카카오닉네임")).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded-random-password");
        given(memberRepository.saveAndFlush(any())).willAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        given(tokenIssuer.issue(any())).willReturn(tokens());

        KakaoAuthResponse response = service.authenticate(request("verification-token"));

        assertThat(response.isNewMember()).isTrue();
        assertThat(response.member().memberId()).isEqualTo(10L);
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).saveAndFlush(memberCaptor.capture());
        assertThat(memberCaptor.getValue().getProfileImageUrl()).isEqualTo("https://image.example/profile.jpg");
        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token", PhoneVerificationPurpose.SIGN_UP, "01012345678"
        );
        verify(socialAccountRepository).save(any(SocialAccount.class));
    }

    @Test
    void linksExistingMemberWithSameVerifiedPhone() {
        Member member = member(1L, "user@example.com", "01012345678");
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.of(member));
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.of(member));
        given(tokenIssuer.issue(member)).willReturn(tokens());

        KakaoAuthResponse response = service.authenticate(request("verification-token"));

        assertThat(response.isNewMember()).isFalse();
        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token", PhoneVerificationPurpose.SIGN_UP, "01012345678"
        );
        verify(socialAccountRepository).save(any(SocialAccount.class));
    }

    @Test
    void requiresAccountLinkWhenEmailBelongsToDifferentIdentity() {
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.empty());
        given(memberRepository.findByEmail("user@example.com"))
                .willReturn(Optional.of(member(1L, "user@example.com", "01099998888")));

        assertThatThrownBy(() -> service.authenticate(request("verification-token")))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ACCOUNT_LINK_REQUIRED));

        verifyNoInteractions(phoneVerificationService, tokenIssuer, refreshSessionStore);
    }

    @Test
    void requiresPhoneVerificationForNewMember() {
        given(socialAccountRepository.findByProviderAndProviderUserId(any(), any()))
                .willReturn(Optional.empty());
        given(memberRepository.findByPhoneNumber("01012345678")).willReturn(Optional.empty());
        given(memberRepository.findByEmail("user@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(request(null)))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION));

        verifyNoInteractions(tokenIssuer, refreshSessionStore);
    }

    private KakaoAuthRequest request(String verificationToken) {
        return new KakaoAuthRequest(
                " authorization-code ",
                " http://localhost/callback ",
                verificationToken,
                " device-id "
        );
    }

    private KakaoProfile profile() {
        return new KakaoProfile(
                "12345",
                "user@example.com",
                "카카오닉네임",
                "https://image.example/profile.jpg",
                "홍길동",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                "01012345678"
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
