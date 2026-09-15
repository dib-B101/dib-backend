package com.b101.dib.auth.command.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

import com.b101.dib.auth.command.dto.PasswordResetLinkRequest;
import com.b101.dib.auth.command.dto.PasswordResetRequest;
import com.b101.dib.auth.command.event.PasswordChangedEvent;
import com.b101.dib.auth.config.PasswordResetProperties;
import com.b101.dib.auth.domain.PasswordPolicy;
import com.b101.dib.auth.domain.PhoneNumber;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.email.EmailSender;
import com.b101.dib.auth.repository.PasswordResetTokenStore;
import com.b101.dib.auth.repository.RefreshSessionStore;
import com.b101.dib.auth.token.PasswordResetTokenHasher;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_BYTES = 32;

    private final MemberRepository memberRepository;
    private final PhoneVerificationService phoneVerificationService;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final PasswordResetTokenHasher tokenHasher;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final RefreshSessionStore refreshSessionStore;
    private final PasswordResetProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void requestResetLink(PasswordResetLinkRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String phoneNumber = PhoneNumber.from(request.phoneNumber()).value();

        phoneVerificationService.consumeVerificationToken(
                request.phoneVerificationToken(),
                PhoneVerificationPurpose.RESET_PASSWORD,
                phoneNumber
        );

        Member member = memberRepository.findByEmailAndPhoneNumber(email, phoneNumber).orElse(null);
        if (!canResetPassword(member)) {
            return;
        }

        String resetToken = generateToken();
        passwordResetTokenStore.save(
                tokenHasher.hash(resetToken),
                member.getId(),
                properties.tokenTtl()
        );
        emailSender.sendPasswordResetLink(email, resetLink(resetToken));
    }

    @Override
    @Transactional
    public void resetPassword(PasswordResetRequest request) {
        if (!PasswordPolicy.isValid(request.newPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        Long memberId = passwordResetTokenStore.consume(
                tokenHasher.hash(request.resetToken().trim())
        );
        Member member = memberRepository.findById(memberId).orElse(null);
        if (!canResetPassword(member)) {
            throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
        }

        Instant changedAt = clock.instant();
        member.setPassword(passwordEncoder.encode(request.newPassword()));
        member.setUpdatedAt(LocalDateTime.ofInstant(changedAt, ZoneOffset.UTC));
        memberRepository.save(member);
        refreshSessionStore.revokeAll(memberId);
        eventPublisher.publishEvent(new PasswordChangedEvent(
                UUID.randomUUID().toString(),
                memberId,
                changedAt
        ));
    }

    private boolean canResetPassword(Member member) {
        return member != null
                && member.getStatus() == MemberStatus.ACTIVE
                && member.getDeletedAt() == null;
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resetLink(String resetToken) {
        return properties.resetPageUrl() + "?token=" + resetToken;
    }
}
