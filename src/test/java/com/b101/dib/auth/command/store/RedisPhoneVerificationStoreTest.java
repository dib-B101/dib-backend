package com.b101.dib.auth.command.store;

import java.time.Duration;
import java.time.Instant;

import com.b101.dib.auth.command.config.PhoneVerificationProperties;
import com.b101.dib.auth.command.dto.PhoneVerificationPurpose;
import com.b101.dib.auth.command.exception.InvalidVerificationCodeException;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.exception.RateLimitExceededException;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisPhoneVerificationStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private DefaultRedisScript<Long> requestScript;
    @Mock
    private DefaultRedisScript<Long> cancelScript;
    @Mock
    private DefaultRedisScript<Long> confirmScript;

    private RedisPhoneVerificationStore store;

    @BeforeEach
    void setUp() {
        PhoneVerificationProperties properties = new PhoneVerificationProperties(
                Duration.ofMinutes(3), Duration.ofMinutes(10), Duration.ofSeconds(60),
                Duration.ofHours(1), 5, 5, "secret"
        );
        store = new RedisPhoneVerificationStore(
                redisTemplate, properties, requestScript, cancelScript, confirmScript
        );
    }

    @Test
    void reservesOtpForThreeMinutes() {
        Instant requestedAt = Instant.parse("2026-09-08T09:00:00Z");
        given(redisTemplate.execute(eq(requestScript), anyList(), any(Object[].class)))
                .willReturn(0L);

        PhoneVerificationReservation reservation = store.reserve(
                "verification-id", PhoneVerificationPurpose.SIGN_UP, "phone-hash", "code-hash", requestedAt
        );

        assertThat(reservation.expiresAt()).isEqualTo(requestedAt.plusSeconds(180));
        verify(redisTemplate).execute(
                eq(requestScript),
                eq(java.util.List.of(
                        "auth:otp:SIGN_UP:phone-hash",
                        "rate:phone-verification:phone-hash:496905"
                )),
                eq("verification-id"),
                eq("code-hash"),
                eq("1788858000"),
                eq("180"),
                eq("60"),
                eq("5"),
                eq("3610")
        );
    }

    @Test
    void rejectsRequestDuringCooldown() {
        given(redisTemplate.execute(eq(requestScript), anyList(), any(Object[].class)))
                .willReturn(45L);

        assertThatThrownBy(() -> store.reserve(
                "verification-id",
                PhoneVerificationPurpose.SIGN_UP,
                "phone-hash",
                "code-hash",
                Instant.parse("2026-09-08T09:00:00Z")
        ))
                .isInstanceOfSatisfying(RateLimitExceededException.class,
                        exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(45));
    }

    @Test
    void rejectsRequestWhenHourlyLimitIsExceeded() {
        given(redisTemplate.execute(eq(requestScript), anyList(), any(Object[].class)))
                .willReturn(-120L);

        assertThatThrownBy(() -> store.reserve(
                "verification-id",
                PhoneVerificationPurpose.SIGN_UP,
                "phone-hash",
                "code-hash",
                Instant.parse("2026-09-08T09:00:00Z")
        ))
                .isInstanceOfSatisfying(RateLimitExceededException.class,
                        exception -> assertThat(exception.getRetryAfterSeconds()).isEqualTo(120));
    }

    @Test
    void confirmsOtpAndStoresVerificationTokenForTenMinutes() {
        Instant confirmedAt = Instant.parse("2026-09-08T09:00:00Z");
        given(redisTemplate.execute(eq(confirmScript), anyList(), any(Object[].class)))
                .willReturn(0L);

        PhoneVerificationConfirmation confirmation = store.confirm(
                "verification-id",
                PhoneVerificationPurpose.SIGN_UP,
                "phone-hash",
                "code-hash",
                "token-hash",
                confirmedAt
        );

        assertThat(confirmation.expiresAt()).isEqualTo(confirmedAt.plusSeconds(600));
        verify(redisTemplate).execute(
                eq(confirmScript),
                eq(java.util.List.of(
                        "auth:otp:SIGN_UP:phone-hash",
                        "auth:verification:token-hash"
                )),
                eq("verification-id"),
                eq("code-hash"),
                eq("5"),
                eq("SIGN_UP"),
                eq("phone-hash"),
                eq("1788858000"),
                eq("600")
        );
    }

    @Test
    void returnsRemainingAttemptsForInvalidCode() {
        given(redisTemplate.execute(eq(confirmScript), anyList(), any(Object[].class)))
                .willReturn(3L);

        assertThatThrownBy(() -> store.confirm(
                "verification-id", PhoneVerificationPurpose.SIGN_UP, "phone-hash",
                "code-hash", "token-hash", Instant.parse("2026-09-08T09:00:00Z")
        ))
                .isInstanceOfSatisfying(InvalidVerificationCodeException.class,
                        exception -> assertThat(exception.getRemainingAttempts()).isEqualTo(3));
    }

    @Test
    void rejectsExpiredVerification() {
        given(redisTemplate.execute(eq(confirmScript), anyList(), any(Object[].class)))
                .willReturn(-1L);

        assertThatThrownBy(() -> store.confirm(
                "verification-id", PhoneVerificationPurpose.SIGN_UP, "phone-hash",
                "code-hash", "token-hash", Instant.parse("2026-09-08T09:00:00Z")
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.VERIFICATION_EXPIRED));
    }

    @Test
    void rejectsVerificationAfterAttemptLimit() {
        given(redisTemplate.execute(eq(confirmScript), anyList(), any(Object[].class)))
                .willReturn(-2L);

        assertThatThrownBy(() -> store.confirm(
                "verification-id", PhoneVerificationPurpose.SIGN_UP, "phone-hash",
                "code-hash", "token-hash", Instant.parse("2026-09-08T09:00:00Z")
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.ATTEMPTS_EXCEEDED));
    }
}
