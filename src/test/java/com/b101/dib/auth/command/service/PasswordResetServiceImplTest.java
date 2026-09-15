package com.b101.dib.auth.command.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.b101.dib.auth.command.dto.PasswordResetLinkRequest;
import com.b101.dib.auth.command.dto.PasswordResetRequest;
import com.b101.dib.auth.command.event.PasswordChangedEvent;
import com.b101.dib.auth.config.PasswordResetProperties;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-15T03:00:00Z");
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private PhoneVerificationService phoneVerificationService;
    @Mock
    private PasswordResetTokenStore tokenStore;
    @Mock
    private EmailSender emailSender;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RefreshSessionStore refreshSessionStore;
    @Mock
    private SecureRandom secureRandom;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final PasswordResetTokenHasher tokenHasher = new PasswordResetTokenHasher();
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetServiceImpl(
                memberRepository,
                phoneVerificationService,
                tokenStore,
                tokenHasher,
                emailSender,
                passwordEncoder,
                refreshSessionStore,
                new PasswordResetProperties(
                        TOKEN_TTL,
                        "http://localhost:5173/password/reset"
                ),
                secureRandom,
                Clock.fixed(NOW, ZoneOffset.UTC),
                eventPublisher
        );
    }

    @Test
    void issuesHashedResetTokenAfterPhoneVerification() {
        Member member = activeMember();
        given(memberRepository.findByEmailAndPhoneNumber(
                "user@example.com", "01012345678"
        )).willReturn(Optional.of(member));
        doAnswer(invocation -> {
            byte[] bytes = invocation.getArgument(0);
            java.util.Arrays.fill(bytes, (byte) 1);
            return null;
        }).when(secureRandom).nextBytes(any(byte[].class));

        service.requestResetLink(new PasswordResetLinkRequest(
                " User@Example.com ",
                "010-1234-5678",
                "verification-token"
        ));

        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token",
                PhoneVerificationPurpose.RESET_PASSWORD,
                "01012345678"
        );
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).sendPasswordResetLink(
                org.mockito.ArgumentMatchers.eq("user@example.com"),
                linkCaptor.capture()
        );
        String resetToken = linkCaptor.getValue().substring(
                linkCaptor.getValue().indexOf("?token=") + 7
        );
        verify(tokenStore).save(tokenHasher.hash(resetToken), 1L, TOKEN_TTL);
    }

    @Test
    void hidesMemberMismatchWithoutIssuingResetToken() {
        given(memberRepository.findByEmailAndPhoneNumber(
                "unknown@example.com", "01012345678"
        )).willReturn(Optional.empty());

        service.requestResetLink(new PasswordResetLinkRequest(
                "unknown@example.com",
                "01012345678",
                "verification-token"
        ));

        verify(phoneVerificationService).consumeVerificationToken(
                "verification-token",
                PhoneVerificationPurpose.RESET_PASSWORD,
                "01012345678"
        );
        verifyNoInteractions(tokenStore, emailSender);
    }

    @Test
    void stopsWhenPhoneVerificationTokenIsInvalid() {
        PasswordResetLinkRequest request = new PasswordResetLinkRequest(
                "user@example.com",
                "01012345678",
                "invalid-verification-token"
        );
        doThrow(new BusinessException(ErrorCode.INVALID_VERIFICATION))
                .when(phoneVerificationService)
                .consumeVerificationToken(
                        "invalid-verification-token",
                        PhoneVerificationPurpose.RESET_PASSWORD,
                        "01012345678"
                );

        assertThatThrownBy(() -> service.requestResetLink(request))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION));

        verifyNoInteractions(memberRepository, tokenStore, emailSender);
    }

    @Test
    void resetsPasswordRevokesSessionsAndPublishesEvent() {
        Member member = activeMember();
        given(tokenStore.consume(tokenHasher.hash("reset-token"))).willReturn(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.encode("NewPassword1!")).willReturn("encoded-password");

        service.resetPassword(new PasswordResetRequest(
                " reset-token ",
                "NewPassword1!"
        ));

        assertThat(member.getPassword()).isEqualTo("encoded-password");
        assertThat(member.getUpdatedAt()).isEqualTo(
                LocalDateTime.ofInstant(NOW, ZoneOffset.UTC)
        );
        verify(memberRepository).save(member);
        verify(refreshSessionStore).revokeAll(1L);
        ArgumentCaptor<PasswordChangedEvent> eventCaptor =
                ArgumentCaptor.forClass(PasswordChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().memberId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().changedAt()).isEqualTo(NOW);
        assertThat(eventCaptor.getValue().eventId()).isNotBlank();
    }

    @Test
    void rejectsInvalidPasswordBeforeConsumingResetToken() {
        assertThatThrownBy(() -> service.resetPassword(
                new PasswordResetRequest("reset-token", "weak-password")
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));

        verifyNoInteractions(tokenStore, memberRepository, passwordEncoder,
                refreshSessionStore, eventPublisher);
    }

    @Test
    void mapsMissingMemberToInvalidResetToken() {
        given(tokenStore.consume(tokenHasher.hash("reset-token"))).willReturn(99L);
        given(memberRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(
                new PasswordResetRequest("reset-token", "NewPassword1!")
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_RESET_TOKEN));

        verifyNoInteractions(passwordEncoder, refreshSessionStore, eventPublisher);
    }

    @Test
    void stopsWhenResetTokenIsInvalid() {
        given(tokenStore.consume(tokenHasher.hash("invalid-token")))
                .willThrow(new BusinessException(ErrorCode.INVALID_RESET_TOKEN));

        assertThatThrownBy(() -> service.resetPassword(
                new PasswordResetRequest("invalid-token", "NewPassword1!")
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_RESET_TOKEN));

        verifyNoInteractions(memberRepository, passwordEncoder,
                refreshSessionStore, eventPublisher);
    }

    private Member activeMember() {
        return Member.builder()
                .id(1L)
                .email("user@example.com")
                .phoneNumber("01012345678")
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
