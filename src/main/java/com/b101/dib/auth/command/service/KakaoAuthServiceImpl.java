package com.b101.dib.auth.command.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;
import com.b101.dib.auth.command.dto.KakaoProfileResponse;
import com.b101.dib.auth.command.dto.KakaoSignupRequest;
import com.b101.dib.auth.command.dto.LoginMemberResponse;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.config.KakaoProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.domain.PhoneNumber;
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
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.domain.SocialAccount;
import com.b101.dib.member.domain.SocialProvider;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.member.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoAuthServiceImpl implements KakaoAuthService {

    private static final SocialProvider PROVIDER = SocialProvider.KAKAO;
    private static final int SIGNUP_TOKEN_BYTES = 32;

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoSignupTokenStore kakaoSignupTokenStore;
    private final KakaoSignupTokenHasher kakaoSignupTokenHasher;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshSessionStore refreshSessionStore;
    private final JwtProperties jwtProperties;
    private final KakaoProperties kakaoProperties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Override
    @Transactional
    public KakaoAuthResponse authenticate(KakaoAuthRequest request) {
        String redirectUri = request.redirectUri().trim();
        validateRedirectUri(redirectUri);
        KakaoProfile profile = kakaoOAuthClient.authenticate(
                request.authorizationCode().trim(),
                redirectUri
        );
        Optional<SocialAccount> linkedAccount = socialAccountRepository
                .findByProviderAndProviderUserId(PROVIDER, profile.providerUserId());
        if (linkedAccount.isPresent()) {
            return login(
                    linkedAccount.get().getMember(),
                    request.deviceId(),
                    false,
                    now()
            );
        }

        String signupToken = generateSignupToken();
        kakaoSignupTokenStore.save(
                kakaoSignupTokenHasher.hash(signupToken),
                new KakaoSignupSession(
                        profile.providerUserId(),
                        profile.nickname(),
                        profile.profileImageUrl()
                ),
                kakaoProperties.signupTokenTtl()
        );
        return new KakaoAuthResponse(
                true,
                null,
                signupToken,
                new KakaoProfileResponse(profile.nickname(), profile.profileImageUrl()),
                null,
                null,
                null
        );
    }

    @Override
    @Transactional
    public KakaoAuthResponse signup(KakaoSignupRequest request) {
        String signupTokenHash = kakaoSignupTokenHasher.hash(request.signupToken().trim());
        KakaoSignupSession signupSession = kakaoSignupTokenStore.find(signupTokenHash);
        Optional<SocialAccount> alreadyLinked = socialAccountRepository
                .findByProviderAndProviderUserId(PROVIDER, signupSession.providerUserId());
        if (alreadyLinked.isPresent()) {
            kakaoSignupTokenStore.consume(signupTokenHash);
            return login(alreadyLinked.get().getMember(), request.deviceId(), false, now());
        }

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String name = request.name().trim();
        String nickname = request.nickname().trim();
        String phoneNumber = PhoneNumber.from(request.phoneNumber()).value();

        Optional<Member> memberByPhone = memberRepository.findByPhoneNumber(phoneNumber);
        Optional<Member> memberByEmail = memberRepository.findByEmail(email);
        Member member;
        boolean isNewMember;
        if (memberByPhone.isPresent()) {
            member = memberByPhone.get();
            if (memberByEmail.isPresent() && !memberByEmail.get().getId().equals(member.getId())) {
                throw new BusinessException(ErrorCode.ACCOUNT_LINK_REQUIRED);
            }
            isNewMember = false;
        } else if (memberByEmail.isPresent()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LINK_REQUIRED);
        } else {
            if (memberRepository.existsByNickname(nickname)) {
                throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
            }
            member = null;
            isNewMember = true;
        }

        phoneVerificationService.consumeVerificationToken(
                request.phoneVerificationToken().trim(),
                PhoneVerificationPurpose.SIGN_UP,
                phoneNumber
        );
        kakaoSignupTokenStore.consume(signupTokenHash);
        if (isNewMember) {
            member = createMember(request, email, name, nickname, phoneNumber, signupSession, now());
        }

        socialAccountRepository.save(SocialAccount.builder()
                .member(member)
                .provider(PROVIDER)
                .providerUserId(signupSession.providerUserId())
                .createdAt(now())
                .build());
        return login(member, request.deviceId(), isNewMember, now());
    }

    private Member createMember(
            KakaoSignupRequest request,
            String email,
            String name,
            String nickname,
            String phoneNumber,
            KakaoSignupSession signupSession,
            LocalDateTime now
    ) {
        Member member = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(nickname)
                .profileImageUrl(signupSession.profileImageUrl())
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
        return memberRepository.saveAndFlush(member);
    }

    private String generateSignupToken() {
        byte[] bytes = new byte[SIGNUP_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void validateRedirectUri(String redirectUri) {
        if (kakaoProperties.redirectUris() == null || kakaoProperties.redirectUris().stream()
                .map(String::trim)
                .noneMatch(redirectUri::equals)) {
            throw new BusinessException(ErrorCode.INVALID_KAKAO_REDIRECT_URI);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    private KakaoAuthResponse login(
            Member member,
            String rawDeviceId,
            boolean isNewMember,
            LocalDateTime now
    ) {
        validateAccountStatus(member);
        String deviceId = rawDeviceId.trim();
        member.setLastLoginAt(now);
        member.setUpdatedAt(now);

        AuthTokenPair tokens = tokenIssuer.issue(member);
        refreshSessionStore.save(member.getId(), deviceId, tokens);
        return new KakaoAuthResponse(
                isNewMember,
                new LoginMemberResponse(
                        member.getId(),
                        member.getEmail(),
                        member.getNickname(),
                        member.getStatus(),
                        member.getRole()
                ),
                null,
                null,
                tokens.accessToken(),
                tokens.refreshToken(),
                jwtProperties.accessTokenValiditySeconds()
        );
    }

    private void validateAccountStatus(Member member) {
        if (member.getStatus() == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }
        if (member.getStatus() == MemberStatus.EXPELLED) {
            throw new BusinessException(ErrorCode.ACCOUNT_BLOCKED);
        }
        if (member.getStatus() != MemberStatus.ACTIVE || member.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }
}
