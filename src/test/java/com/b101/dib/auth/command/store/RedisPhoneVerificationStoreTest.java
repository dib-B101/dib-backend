package com.b101.dib.auth.command.store;

import java.time.Duration;
import java.time.Instant;

import com.b101.dib.auth.command.config.PhoneVerificationProperties;
import com.b101.dib.auth.command.dto.PhoneVerificationPurpose;
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

    private RedisPhoneVerificationStore store;

    @BeforeEach
    void setUp() {
        PhoneVerificationProperties properties = new PhoneVerificationProperties(
                Duration.ofMinutes(3), Duration.ofSeconds(60), Duration.ofHours(1), 5, "secret"
        );
        store = new RedisPhoneVerificationStore(redisTemplate, properties, requestScript, cancelScript);
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
}
