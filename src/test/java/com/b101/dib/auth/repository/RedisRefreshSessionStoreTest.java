package com.b101.dib.auth.repository;

import java.time.Duration;
import java.time.Instant;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedisRefreshSessionStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private DefaultRedisScript<Long> saveScript;
    @Mock
    private DefaultRedisScript<Long> rotateScript;
    @Mock
    private DefaultRedisScript<Long> revokeScript;

    private RedisRefreshSessionStore store;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
                1800,
                Duration.ofDays(30),
                Duration.ofDays(90)
        );
        store = new RedisRefreshSessionStore(
                redisTemplate,
                properties,
                saveScript,
                rotateScript,
                revokeScript
        );
    }

    @Test
    void storesHashedRefreshSessionForThirtyDays() {
        Instant issuedAt = Instant.parse("2026-09-09T09:00:00Z");
        AuthTokenPair tokens = new AuthTokenPair(
                "access", "refresh", "refresh-hash", "family-id",
                issuedAt, issuedAt.plus(Duration.ofDays(90))
        );
        given(redisTemplate.execute(eq(saveScript), anyList(), any(Object[].class)))
                .willReturn(1L);

        store.save(1L, "device-id", tokens);

        verify(redisTemplate).execute(
                eq(saveScript),
                eq(java.util.List.of(
                        "session:refresh:1:device-id",
                        "session:refresh:lookup:refresh-hash"
                )),
                eq("refresh-hash"),
                eq("family-id"),
                eq("1788944400"),
                eq("1796720400"),
                eq("2592000"),
                eq("1"),
                eq("device-id"),
                eq("session:refresh:lookup:"),
                eq("7776000")
        );
    }
}
