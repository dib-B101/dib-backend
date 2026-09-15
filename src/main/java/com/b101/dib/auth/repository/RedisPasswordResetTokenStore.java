package com.b101.dib.auth.repository;

import java.time.Duration;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisPasswordResetTokenStore implements PasswordResetTokenStore {

    private static final String KEY_PREFIX = "auth:password-reset:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String tokenHash, Long memberId, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + tokenHash, memberId.toString(), ttl);
    }

    @Override
    public Long consume(String tokenHash) {
        String memberId = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + tokenHash);
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
        }

        try {
            return Long.valueOf(memberId);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_RESET_TOKEN);
        }
    }
}
