package com.b101.dib.auth.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        DefaultRedisScript<Long> rotateScript = new DefaultRedisScript<>();
        rotateScript.setLocation(new ClassPathResource("redis/rotate-refresh-session.lua"));
        rotateScript.setResultType(Long.class);
        DefaultRedisScript<Long> revokeScript = new DefaultRedisScript<>();
        revokeScript.setLocation(new ClassPathResource("redis/revoke-refresh-session.lua"));
        revokeScript.setResultType(Long.class);
        store = new RedisRefreshSessionStore(
                redisTemplate,
                properties,
                saveScript,
                rotateScript,
                revokeScript
        );
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
                .containsEntry("absoluteExpiresAt", "1796778060")
                .containsEntry("memberId", "1")
                .containsEntry("deviceId", "device-id");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS))
                .isBetween(INACTIVITY.toSeconds() - 10, INACTIVITY.toSeconds());
        assertThat(redisTemplate.opsForHash().entries("session:refresh:lookup:first-hash"))
                .containsEntry("status", "REVOKED");
        assertThat(redisTemplate.opsForHash().entries("session:refresh:lookup:next-hash"))
                .containsEntry("sessionKey", key)
                .containsEntry("status", "ACTIVE");
    }

    @Test
    void rotatesRefreshTokenAndRejectsReusedTokenFamily() {
        Instant issuedAt = Instant.now();
        AuthTokenPair first = tokens("first-hash", "family-id", issuedAt);
        store.save(1L, "device-id", first);

        RefreshSession session = store.findForRefresh(
                "first-hash",
                "device-id",
                issuedAt.plusSeconds(10)
        );
        AuthTokenPair rotated = new AuthTokenPair(
                "next-access",
                "next-refresh",
                "next-hash",
                "family-id",
                issuedAt.plusSeconds(10),
                first.absoluteExpiresAt()
        );
        store.rotate(session, "first-hash", rotated);

        assertThat(redisTemplate.opsForHash().entries("session:refresh:1:device-id"))
                .containsEntry("tokenHash", "next-hash")
                .containsEntry("familyId", "family-id");

        assertThatThrownBy(() -> store.findForRefresh(
                "first-hash",
                "device-id",
                issuedAt.plusSeconds(20)
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.SESSION_REVOKED));

        assertThatThrownBy(() -> store.findForRefresh(
                "next-hash",
                "device-id",
                issuedAt.plusSeconds(20)
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                .isEqualTo(ErrorCode.SESSION_REVOKED));
    }

    @Test
    void returnsExpiredWhenInactivityOrAbsoluteLimitIsReached() {
        Instant issuedAt = Instant.now();
        AuthTokenPair tokens = tokens("refresh-hash", "family-id", issuedAt);
        store.save(1L, "device-id", tokens);
        redisTemplate.delete("session:refresh:1:device-id");

        assertThatThrownBy(() -> store.findForRefresh(
                "refresh-hash",
                "device-id",
                issuedAt.plusSeconds(10)
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED));

        store.save(1L, "device-id", tokens);
        assertThatThrownBy(() -> store.findForRefresh(
                "refresh-hash",
                "device-id",
                tokens.absoluteExpiresAt()
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED));
    }

    @Test
    void rejectsDifferentDeviceAndRevokesSessionOnLogout() {
        Instant issuedAt = Instant.now();
        store.save(1L, "device-id", tokens("refresh-hash", "family-id", issuedAt));

        assertThatThrownBy(() -> store.findForRefresh(
                "refresh-hash",
                "different-device",
                issuedAt.plusSeconds(10)
        )).isInstanceOfSatisfying(BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.DEVICE_MISMATCH));

        store.revoke(1L, "device-id");

        assertThat(redisTemplate.hasKey("session:refresh:1:device-id")).isFalse();
        assertThat(redisTemplate.opsForHash().entries("session:refresh:lookup:refresh-hash"))
                .containsEntry("status", "REVOKED");
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
