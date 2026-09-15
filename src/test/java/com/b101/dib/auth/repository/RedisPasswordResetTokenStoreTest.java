package com.b101.dib.auth.repository;

import java.time.Duration;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisPasswordResetTokenStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private PasswordResetTokenStore store;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        store = new RedisPasswordResetTokenStore(redisTemplate);
    }

    @Test
    void storesOnlyHashedTokenForThirtyMinutes() {
        store.save("token-hash", 1L, Duration.ofMinutes(30));

        verify(valueOperations).set(
                "auth:password-reset:token-hash",
                "1",
                Duration.ofMinutes(30)
        );
    }

    @Test
    void consumesTokenOnlyOnce() {
        given(valueOperations.getAndDelete("auth:password-reset:token-hash"))
                .willReturn("1");

        assertThat(store.consume("token-hash")).isEqualTo(1L);
    }

    @Test
    void rejectsMissingToken() {
        given(valueOperations.getAndDelete("auth:password-reset:token-hash"))
                .willReturn(null);

        assertThatThrownBy(() -> store.consume("token-hash"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_RESET_TOKEN));
    }
}
