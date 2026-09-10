package com.b101.dib.auth.command.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.b101.dib.auth.config.PhoneVerificationProperties;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.repository.PhoneVerificationConfirmation;
import com.b101.dib.auth.repository.PhoneVerificationReservation;
import com.b101.dib.auth.repository.PhoneVerificationStore;
import com.b101.dib.auth.sms.SmsSender;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PhoneVerificationServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-08T09:00:00Z");

    @Mock
    private PhoneVerificationStore phoneVerificationStore;
    @Mock
    private SmsSender smsSender;
    @Mock
    private SecureRandom secureRandom;

    private PhoneVerificationServiceImpl phoneVerificationService;

    @BeforeEach
    void setUp() {
        PhoneVerificationProperties properties = new PhoneVerificationProperties(
                Duration.ofMinutes(3), Duration.ofMinutes(10), Duration.ofSeconds(60),
                Duration.ofHours(1), 5, 5, "test-hmac-secret"
        );
        phoneVerificationService = new PhoneVerificationServiceImpl(
                phoneVerificationStore,
                smsSender,
                properties,
                secureRandom,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void normalizesPhoneNumberAndRequestsVerification() {
        given(secureRandom.nextInt(1_000_000)).willReturn(42);
        given(phoneVerificationStore.reserve(anyString(), any(), anyString(), anyString(), any()))
                .willReturn(new PhoneVerificationReservation(NOW.plusSeconds(180)));

        PhoneVerificationResponse response = phoneVerificationService.request(
                new PhoneVerificationRequest(" 010-1234-5678 ", PhoneVerificationPurpose.SIGN_UP)
        );

        verify(smsSender).send("01012345678", "[DIB] 인증번호는 000042입니다. 3분 이내에 입력해주세요.");
        assertThat(response.expiresAt()).isEqualTo(OffsetDateTime.parse("2026-09-08T18:03:00+09:00"));
        assertThat(response.retryAfterSeconds()).isEqualTo(60);
        assertThat(response.verificationId()).contains(".");
    }

    @Test
    void rejectsUnsupportedPhoneNumber() {
        assertThatThrownBy(() -> phoneVerificationService.request(
                new PhoneVerificationRequest("02-1234-5678", PhoneVerificationPurpose.SIGN_UP)
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PHONE));

        verifyNoInteractions(phoneVerificationStore, smsSender);
    }

    @Test
    void rejectsMissingPurpose() {
        assertThatThrownBy(() -> phoneVerificationService.request(
                new PhoneVerificationRequest("01012345678", null)
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));

        verifyNoInteractions(phoneVerificationStore, smsSender);
    }

    @Test
    void cancelsReservationWhenSmsDeliveryFails() {
        given(secureRandom.nextInt(1_000_000)).willReturn(123456);
        given(phoneVerificationStore.reserve(anyString(), any(), anyString(), anyString(), any()))
                .willReturn(new PhoneVerificationReservation(NOW.plusSeconds(180)));
        doThrow(new IllegalStateException("SMS failure"))
                .when(smsSender).send(anyString(), anyString());

        assertThatThrownBy(() -> phoneVerificationService.request(
                new PhoneVerificationRequest("01012345678", PhoneVerificationPurpose.SIGN_UP)
        )).isInstanceOf(IllegalStateException.class);

        verify(phoneVerificationStore).cancel(anyString(), any(), anyString());
    }

    @Test
    void confirmsCodeAndReturnsOneTimeVerificationToken() {
        given(secureRandom.nextInt(1_000_000)).willReturn(42);
        given(phoneVerificationStore.reserve(anyString(), any(), anyString(), anyString(), any()))
                .willReturn(new PhoneVerificationReservation(NOW.plusSeconds(180)));

        PhoneVerificationResponse requested = phoneVerificationService.request(
                new PhoneVerificationRequest("01012345678", PhoneVerificationPurpose.SIGN_UP)
        );
        given(phoneVerificationStore.confirm(
                anyString(), any(), anyString(), anyString(), anyString(), any()
        )).willReturn(new PhoneVerificationConfirmation(NOW.plusSeconds(600)));

        PhoneVerificationConfirmResponse response = phoneVerificationService.confirm(
                requested.verificationId(),
                new PhoneVerificationConfirmRequest("000042")
        );

        assertThat(response.verificationToken()).isNotBlank();
        assertThat(response.expiresAt()).isEqualTo(
                OffsetDateTime.parse("2026-09-08T18:10:00+09:00")
        );
        verify(phoneVerificationStore).confirm(
                org.mockito.ArgumentMatchers.eq(requested.verificationId()),
                org.mockito.ArgumentMatchers.eq(PhoneVerificationPurpose.SIGN_UP),
                anyString(),
                anyString(),
                anyString(),
                org.mockito.ArgumentMatchers.eq(NOW)
        );
    }

    @Test
    void rejectsTamperedVerificationId() {
        assertThatThrownBy(() -> phoneVerificationService.confirm(
                "tampered.verification-id",
                new PhoneVerificationConfirmRequest("123456")
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION_ID));

        verifyNoInteractions(phoneVerificationStore, smsSender);
    }

    @Test
    void consumesVerificationTokenForNormalizedPhoneNumber() {
        phoneVerificationService.consumeVerificationToken(
                "verification-token",
                PhoneVerificationPurpose.SIGN_UP,
                "010-1234-5678"
        );

        verify(phoneVerificationStore).consume(
                anyString(),
                org.mockito.ArgumentMatchers.eq(PhoneVerificationPurpose.SIGN_UP),
                anyString()
        );
    }

    @Test
    void rejectsBlankVerificationToken() {
        assertThatThrownBy(() -> phoneVerificationService.consumeVerificationToken(
                " ", PhoneVerificationPurpose.SIGN_UP, "01012345678"
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION));

        verifyNoInteractions(phoneVerificationStore, smsSender);
    }
}
