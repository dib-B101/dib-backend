package com.b101.dib.auth.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RedisRefreshSessionStoreIntegrationTest {

    private static final int REDIS_PORT = 6379;
    private static final Duration INACTIVITY = Duration.ofDays(30);

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(REDIS_PORT);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private RedisRefreshSessionStore store;

    @BeforeAll
    static void setUpRedis() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                REDIS.getHost(), REDIS.getMappedPort(REDIS_PORT)
        );
        connectionFactory = new LettuceConnectionFactory(configuration);
        connectionFactory.afterPropertiesSet();
        connectionFactory.start();

        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
    }

    @AfterAll
    static void closeRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
    }

    @BeforeEach
    void setUp() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }

        JwtProperties properties = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256",
                1800,
                INACTIVITY,
                Duration.ofDays(90)
        );
        DefaultRedisScript<Long> saveScript = new DefaultRedisScript<>();
        saveScript.setLocation(new ClassPathResource("redis/save-refresh-session.lua"));
        saveScript.setResultType(Long.class);
        store = new RedisRefreshSessionStore(redisTemplate, properties, saveScript);
    }

    @Test
    void replacesRefreshSessionForSameMemberAndDevice() {
        String key = "session:refresh:1:device-id";
        Instant firstIssuedAt = Instant.parse("2026-09-10T01:00:00Z");
        AuthTokenPair firstTokens = tokens(
                "first-hash", "first-family", firstIssuedAt
        );
        AuthTokenPair nextTokens = tokens(
                "next-hash", "next-family", firstIssuedAt.plusSeconds(60)
        );

        store.save(1L, "device-id", firstTokens);
        store.save(1L, "device-id", nextTokens);

        Map<Object, Object> session = redisTemplate.opsForHash().entries(key);
        assertThat(session)
                .containsEntry("tokenHash", "next-hash")
                .containsEntry("familyId", "next-family")
                .containsEntry("issuedAt", "1789002060")
                .containsEntry("lastUsedAt", "1789002060")
                .containsEntry("absoluteExpiresAt", "1796778060");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS))
                .isBetween(INACTIVITY.toSeconds() - 10, INACTIVITY.toSeconds());
    }

    private AuthTokenPair tokens(String tokenHash, String familyId, Instant issuedAt) {
        return new AuthTokenPair(
                "access-token",
                "refresh-token",
                tokenHash,
                familyId,
                issuedAt,
                issuedAt.plus(Duration.ofDays(90))
        );
    }
}
