package com.b101.dib.auth.command.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;
import com.b101.dib.auth.command.dto.LoginMemberResponse;
import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.external.kakao.KakaoOAuthClient;
import com.b101.dib.auth.external.kakao.KakaoProfile;
import com.b101.dib.auth.repository.RefreshSessionStore;
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

    private final KakaoOAuthClient kakaoOAuthClient;
    private final SocialAccountRepository socialAccountRepository;
    private final MemberRepository memberRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;
    private final RefreshSessionStore refreshSessionStore;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Override
    @Transactional
    public KakaoAuthResponse authenticate(KakaoAuthRequest request) {
        KakaoProfile profile = kakaoOAuthClient.authenticate(
                request.authorizationCode().trim(),
                request.redirectUri().trim()
        );
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);

        Optional<SocialAccount> linkedAccount = socialAccountRepository
                .findByProviderAndProviderUserId(PROVIDER, profile.providerUserId());
        if (linkedAccount.isPresent()) {
            return login(linkedAccount.get().getMember(), request.deviceId(), false, now);
        }

        Optional<Member> memberByPhone = memberRepository.findByPhoneNumber(profile.phoneNumber());
        Optional<Member> memberByEmail = memberRepository.findByEmail(profile.email());
        Member member;
        boolean isNewMember;
        if (memberByPhone.isPresent()) {
            member = memberByPhone.get();
            if (memberByEmail.isPresent() && !memberByEmail.get().getId().equals(member.getId())) {
                throw new BusinessException(ErrorCode.ACCOUNT_LINK_REQUIRED);
            }
            requireAndConsumePhoneVerification(request.phoneVerificationToken(), profile.phoneNumber());
            isNewMember = false;
        } else if (memberByEmail.isPresent()) {
            throw new BusinessException(ErrorCode.ACCOUNT_LINK_REQUIRED);
        } else {
            requireAndConsumePhoneVerification(request.phoneVerificationToken(), profile.phoneNumber());
            member = createMember(profile, now);
            isNewMember = true;
        }

        socialAccountRepository.save(SocialAccount.builder()
                .member(member)
                .provider(PROVIDER)
                .providerUserId(profile.providerUserId())
                .createdAt(now)
                .build());
        return login(member, request.deviceId(), isNewMember, now);
    }

    private Member createMember(KakaoProfile profile, LocalDateTime now) {
        Member member = Member.builder()
                .email(profile.email())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nickname(uniqueNickname(profile.nickname(), profile.providerUserId()))
                .profileImageUrl(profile.profileImageUrl())
                .name(profile.name().substring(0, Math.min(profile.name().length(), 10)))
                .gender(profile.gender())
                .birthDate(profile.birthDate())
                .phoneNumber(profile.phoneNumber())
                .status(MemberStatus.ACTIVE)
                .role(MemberRole.USER)
                .score(50.0)
                .createdAt(now)
                .updatedAt(now)
                .warningCount(0)
                .build();
        return memberRepository.saveAndFlush(member);
    }

    private String uniqueNickname(String nickname, String providerUserId) {
        String base = nickname.length() <= 50 ? nickname : nickname.substring(0, 50);
        if (!memberRepository.existsByNickname(base)) {
            return base;
        }
        String suffix = "_" + providerUserId;
        int baseLength = Math.max(0, 50 - suffix.length());
        return base.substring(0, Math.min(base.length(), baseLength)) + suffix;
    }

    private void requireAndConsumePhoneVerification(String token, String phoneNumber) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
        }
        phoneVerificationService.consumeVerificationToken(
                token.trim(),
                PhoneVerificationPurpose.SIGN_UP,
                phoneNumber
        );
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
