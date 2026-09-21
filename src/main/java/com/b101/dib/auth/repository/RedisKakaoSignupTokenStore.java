package com.b101.dib.auth.repository;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisKakaoSignupTokenStore implements KakaoSignupTokenStore {

    private static final String KEY_PREFIX = "auth:kakao-signup:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void save(String tokenHash, KakaoSignupSession session, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + tokenHash, encode(session), ttl);
    }

    @Override
    public KakaoSignupSession find(String tokenHash) {
        return decodeSession(redisTemplate.opsForValue().get(KEY_PREFIX + tokenHash));
    }

    @Override
    public KakaoSignupSession consume(String tokenHash) {
        return decodeSession(redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + tokenHash));
    }

    private KakaoSignupSession decodeSession(String value) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_KAKAO_SIGNUP_TOKEN);
        }
        try {
            String[] fields = value.split("\\.", -1);
            if (fields.length != 3 || fields[0].isBlank()) {
                throw new IllegalArgumentException("invalid Kakao signup session");
            }
            return new KakaoSignupSession(
                    fields[0],
                    decode(fields[1]),
                    decode(fields[2])
            );
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_KAKAO_SIGNUP_TOKEN);
        }
    }

    private String encode(KakaoSignupSession session) {
        return session.providerUserId()
                + "." + encode(session.nickname())
                + "." + encode(session.profileImageUrl());
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        if (value.isEmpty()) {
            return null;
        }
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
