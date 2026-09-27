package com.b101.dib.auth.command.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import com.b101.dib.auth.command.dto.FirebasePhoneVerificationRequest;
import com.b101.dib.auth.config.PhoneVerificationProperties;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FirebasePhoneVerificationServiceTest {
    private static final Instant NOW = Instant.parse("2026-09-27T09:00:00Z");

    @Mock private FirebasePhoneTokenVerifier verifier;
    @Mock private StringRedisTemplate redis;
    @Mock private DefaultRedisScript<Long> script;
    private FirebasePhoneVerificationService service;

    @BeforeEach
    void setUp() {
        service = new FirebasePhoneVerificationService(verifier, redis,
                new PhoneVerificationProperties(Duration.ofMinutes(3), Duration.ofMinutes(10),
                        Duration.ofSeconds(60), Duration.ofHours(1), 5, 5, "", "test-secret"),
                new SecureRandom(), Clock.fixed(NOW, ZoneOffset.UTC), script);
    }

    @Test
    void issuesPurposeBoundTokenForRecentlyVerifiedMatchingPhone() {
        given(verifier.verify("id-token")).willReturn(
                new FirebasePhoneTokenVerifier.VerifiedPhone("firebase-user", "+821012345678", NOW.minusSeconds(30)));
        given(redis.execute(eq(script), any(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .willReturn(1L);

        var response = service.verify(new FirebasePhoneVerificationRequest(
                "id-token", "010-1234-5678", PhoneVerificationPurpose.SIGN_UP));

        assertThat(response.verificationToken()).isNotBlank();
        assertThat(response.expiresAt().toInstant()).isEqualTo(NOW.plusSeconds(600));
    }

    @Test
    void rejectsDifferentPhoneBeforeWritingToken() {
        given(verifier.verify("id-token")).willReturn(
                new FirebasePhoneTokenVerifier.VerifiedPhone("firebase-user", "+821087654321", NOW));

        assertThatThrownBy(() -> service.verify(new FirebasePhoneVerificationRequest(
                "id-token", "01012345678", PhoneVerificationPurpose.SIGN_UP)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(redis);
    }

    @Test
    void rejectsStaleAuthenticationBeforeWritingToken() {
        given(verifier.verify("id-token")).willReturn(
                new FirebasePhoneTokenVerifier.VerifiedPhone("firebase-user", "+821012345678", NOW.minusSeconds(301)));

        assertThatThrownBy(() -> service.verify(new FirebasePhoneVerificationRequest(
                "id-token", "01012345678", PhoneVerificationPurpose.RESET_PASSWORD)))
                .isInstanceOf(BusinessException.class);
        verifyNoInteractions(redis);
    }
}
