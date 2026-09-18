package com.b101.dib.auth.repository;

import java.time.Duration;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisKakaoSignupTokenStoreTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private KakaoSignupTokenStore store;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        store = new RedisKakaoSignupTokenStore(redisTemplate);
    }

    @Test
    void storesAndConsumesKakaoSignupSession() {
        KakaoSignupSession session = new KakaoSignupSession(
                "12345",
                "카카오닉네임",
                "https://image.example/profile.jpg"
        );
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        store.save("token-hash", session, Duration.ofMinutes(10));

        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("auth:kakao-signup:token-hash"),
                valueCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(Duration.ofMinutes(10))
        );
        given(valueOperations.getAndDelete("auth:kakao-signup:token-hash"))
                .willReturn(valueCaptor.getValue());
        assertThat(store.consume("token-hash")).isEqualTo(session);
    }

    @Test
    void rejectsExpiredOrReusedSignupToken() {
        given(valueOperations.getAndDelete("auth:kakao-signup:token-hash"))
                .willReturn(null);

        assertThatThrownBy(() -> store.consume("token-hash"))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_KAKAO_SIGNUP_TOKEN));
    }
}
