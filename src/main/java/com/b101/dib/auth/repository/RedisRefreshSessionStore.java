package com.b101.dib.auth.repository;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.b101.dib.auth.config.JwtProperties;
import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class RedisRefreshSessionStore implements RefreshSessionStore {

    private static final String SESSION_PREFIX = "session:refresh:";
    private static final String LOOKUP_PREFIX = "session:refresh:lookup:";
    private static final String ACTIVE = "ACTIVE";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties properties;
    private final DefaultRedisScript<Long> saveScript;
    private final DefaultRedisScript<Long> rotateScript;
    private final DefaultRedisScript<Long> revokeScript;

    public RedisRefreshSessionStore(
            StringRedisTemplate redisTemplate,
            JwtProperties properties,
            @Qualifier("saveRefreshSessionScript") DefaultRedisScript<Long> saveScript,
            @Qualifier("rotateRefreshSessionScript") DefaultRedisScript<Long> rotateScript,
            @Qualifier("revokeRefreshSessionScript") DefaultRedisScript<Long> revokeScript
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.saveScript = saveScript;
        this.rotateScript = rotateScript;
        this.revokeScript = revokeScript;
    }

    @Override
    public void save(Long memberId, String deviceId, AuthTokenPair tokens) {
        long lookupTtl = remainingSeconds(tokens.issuedAt(), tokens.absoluteExpiresAt());
        long sessionTtl = Math.min(properties.refreshTokenInactivity().toSeconds(), lookupTtl);
        String sessionKey = sessionKey(memberId, deviceId);
        String lookupKey = lookupKey(tokens.refreshTokenHash());

        Long result = redisTemplate.execute(
                saveScript,
                List.of(sessionKey, lookupKey),
                tokens.refreshTokenHash(),
                tokens.familyId(),
                String.valueOf(tokens.issuedAt().getEpochSecond()),
                String.valueOf(tokens.absoluteExpiresAt().getEpochSecond()),
                String.valueOf(sessionTtl),
                String.valueOf(memberId),
                deviceId,
                LOOKUP_PREFIX,
                String.valueOf(lookupTtl)
        );

        if (result == null || result != 1) {
            throw new IllegalStateException("Redis에 Refresh Token 세션을 저장하지 못했습니다.");
        }
    }

    @Override
    public RefreshSession findForRefresh(
            String presentedTokenHash,
            String deviceId,
            Instant now
    ) {
        Map<Object, Object> lookup = redisTemplate.opsForHash()
                .entries(lookupKey(presentedTokenHash));
        if (lookup.isEmpty()) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }
        if (!ACTIVE.equals(value(lookup, "status"))) {
            revokeSessionKey(value(lookup, "sessionKey"));
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }

        Instant absoluteExpiresAt = parseInstant(lookup, "absoluteExpiresAt");
        if (!now.isBefore(absoluteExpiresAt)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String sessionKey = value(lookup, "sessionKey");
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }
        Map<Object, Object> session = redisTemplate.opsForHash().entries(sessionKey);
        if (session.isEmpty()) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        String storedDeviceId = value(session, "deviceId");
        if (storedDeviceId == null) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }
        if (!deviceId.equals(storedDeviceId)) {
            throw new BusinessException(ErrorCode.DEVICE_MISMATCH);
        }
        if (!presentedTokenHash.equals(value(session, "tokenHash"))) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }

        try {
            Long memberId = Long.valueOf(value(session, "memberId"));
            String familyId = value(session, "familyId");
            if (!sessionKey(memberId, deviceId).equals(sessionKey)
                    || familyId == null
                    || familyId.isBlank()) {
                throw new BusinessException(ErrorCode.SESSION_REVOKED);
            }
            return new RefreshSession(memberId, deviceId, familyId, absoluteExpiresAt);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }
    }

    @Override
    public void rotate(
            RefreshSession session,
            String presentedTokenHash,
            AuthTokenPair tokens
    ) {
        long lookupTtl = remainingSeconds(tokens.issuedAt(), session.absoluteExpiresAt());
        long sessionTtl = Math.min(properties.refreshTokenInactivity().toSeconds(), lookupTtl);
        Long result = redisTemplate.execute(
                rotateScript,
                List.of(
                        lookupKey(presentedTokenHash),
                        lookupKey(tokens.refreshTokenHash()),
                        sessionKey(session.memberId(), session.deviceId())
                ),
                presentedTokenHash,
                tokens.refreshTokenHash(),
                session.familyId(),
                String.valueOf(tokens.issuedAt().getEpochSecond()),
                String.valueOf(session.absoluteExpiresAt().getEpochSecond()),
                String.valueOf(sessionTtl),
                String.valueOf(lookupTtl),
                session.deviceId(),
                LOOKUP_PREFIX
        );

        if (result == null) {
            throw new IllegalStateException("Redis에서 Refresh Token을 교체하지 못했습니다.");
        }
        if (result == 1) {
            return;
        }
        if (result == -3 || result == -5) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (result == -6) {
            throw new BusinessException(ErrorCode.DEVICE_MISMATCH);
        }
        throw new BusinessException(ErrorCode.SESSION_REVOKED);
    }

    @Override
    public void revoke(Long memberId, String deviceId) {
        Long result = redisTemplate.execute(
                revokeScript,
                List.of(sessionKey(memberId, deviceId)),
                LOOKUP_PREFIX
        );
        if (result == null) {
            throw new IllegalStateException("Redis에서 Refresh Token 세션을 제거하지 못했습니다.");
        }
    }

    private String sessionKey(Long memberId, String deviceId) {
        return SESSION_PREFIX + memberId + ":" + deviceId;
    }

    private String lookupKey(String tokenHash) {
        return LOOKUP_PREFIX + tokenHash;
    }

    private void revokeSessionKey(String sessionKey) {
        if (sessionKey == null) {
            return;
        }
        Long result = redisTemplate.execute(
                revokeScript,
                List.of(sessionKey),
                LOOKUP_PREFIX
        );
        if (result == null) {
            throw new IllegalStateException("Redis에서 Refresh Token 세션을 제거하지 못했습니다.");
        }
    }

    private long remainingSeconds(Instant from, Instant until) {
        long seconds = Duration.between(from, until).toSeconds();
        if (seconds <= 0) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        return seconds;
    }

    private Instant parseInstant(Map<Object, Object> values, String field) {
        try {
            return Instant.ofEpochSecond(Long.parseLong(value(values, field)));
        } catch (NumberFormatException | DateTimeException e) {
            throw new BusinessException(ErrorCode.SESSION_REVOKED);
        }
    }

    private String value(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        return value == null ? null : value.toString();
    }
}
