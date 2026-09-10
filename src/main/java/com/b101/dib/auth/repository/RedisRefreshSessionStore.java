package com.b101.dib.auth.repository;

import java.util.List;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRefreshSessionStore implements RefreshSessionStore {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;
    private final DefaultRedisScript<Long> saveScript;

    public RedisRefreshSessionStore(
            StringRedisTemplate redisTemplate,
            JwtProperties properties,
            @Qualifier("saveRefreshSessionScript") DefaultRedisScript<Long> saveScript
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.saveScript = saveScript;
    }

    @Override
    public void save(Long memberId, String deviceId, AuthTokenPair tokens) {
        Long result = redisTemplate.execute(
                saveScript,
                List.of("session:refresh:" + memberId + ":" + deviceId),
                tokens.refreshTokenHash(),
                tokens.familyId(),
                String.valueOf(tokens.issuedAt().getEpochSecond()),
                String.valueOf(tokens.absoluteExpiresAt().getEpochSecond()),
                String.valueOf(properties.refreshTokenInactivity().toSeconds())
        );

        if (result == null || result != 1) {
            throw new IllegalStateException("Redis에 Refresh Token 세션을 저장하지 못했습니다.");
        }
    }
}
