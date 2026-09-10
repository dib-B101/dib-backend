package com.b101.dib.auth.repository;

import java.time.Duration;
import java.util.Map;

import com.b101.dib.auth.config.PhoneVerificationProperties;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
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
class RedisPhoneVerificationStoreIntegrationTest {

    private static final int REDIS_PORT = 6379;

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7.2-alpine")
            .withExposedPorts(REDIS_PORT);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private RedisPhoneVerificationStore store;

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

        PhoneVerificationProperties properties = new PhoneVerificationProperties(
                Duration.ofMinutes(3), Duration.ofMinutes(10), Duration.ofSeconds(60),
                Duration.ofHours(1), 5, 5, "secret"
        );
        DefaultRedisScript<Long> consumeScript = new DefaultRedisScript<>();
        consumeScript.setLocation(new ClassPathResource("redis/consume-phone-verification.lua"));
        consumeScript.setResultType(Long.class);

        store = new RedisPhoneVerificationStore(
                redisTemplate,
                properties,
                new DefaultRedisScript<>(),
                new DefaultRedisScript<>(),
                new DefaultRedisScript<>(),
                consumeScript
        );
    }

    @Test
    void consumesMatchingVerificationTokenOnlyOnce() {
        saveVerification("token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash");

        store.consume("token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash");

        assertThat(redisTemplate.hasKey("auth:verification:token-hash")).isFalse();
        assertThatThrownBy(() -> store.consume(
                "token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash"
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION));
    }

    @Test
    void preservesVerificationTokenWhenPurposeDoesNotMatch() {
        saveVerification("token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash");

        assertThatThrownBy(() -> store.consume(
                "token-hash", PhoneVerificationPurpose.RESET_PASSWORD, "phone-hash"
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION));

        assertThat(redisTemplate.hasKey("auth:verification:token-hash")).isTrue();
        store.consume("token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash");
    }

    @Test
    void preservesVerificationTokenWhenPhoneNumberDoesNotMatch() {
        saveVerification("token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash");

        assertThatThrownBy(() -> store.consume(
                "token-hash", PhoneVerificationPurpose.SIGN_UP, "other-phone-hash"
        ))
                .isInstanceOfSatisfying(BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_VERIFICATION));

        assertThat(redisTemplate.hasKey("auth:verification:token-hash")).isTrue();
        store.consume("token-hash", PhoneVerificationPurpose.SIGN_UP, "phone-hash");
    }

    private void saveVerification(
            String tokenHash,
            PhoneVerificationPurpose purpose,
            String phoneHash
    ) {
        redisTemplate.opsForHash().putAll(
                "auth:verification:" + tokenHash,
                Map.of("purpose", purpose.name(), "phoneHash", phoneHash)
        );
    }
}
