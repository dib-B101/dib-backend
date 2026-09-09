package com.b101.dib.auth.repository;

import java.time.Instant;
import java.util.List;

import com.b101.dib.auth.config.PhoneVerificationProperties;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.exception.InvalidVerificationCodeException;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

@Repository
public class RedisPhoneVerificationStore implements PhoneVerificationStore {

    private final StringRedisTemplate redisTemplate;
    private final PhoneVerificationProperties properties;
    private final DefaultRedisScript<Long> requestScript;
    private final DefaultRedisScript<Long> cancelScript;
    private final DefaultRedisScript<Long> confirmScript;

    public RedisPhoneVerificationStore(
            StringRedisTemplate redisTemplate,
            PhoneVerificationProperties properties,
            @Qualifier("requestPhoneVerificationScript") DefaultRedisScript<Long> requestScript,
            @Qualifier("cancelPhoneVerificationScript") DefaultRedisScript<Long> cancelScript,
            @Qualifier("confirmPhoneVerificationScript") DefaultRedisScript<Long> confirmScript
    ) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.requestScript = requestScript;
        this.cancelScript = cancelScript;
        this.confirmScript = confirmScript;
    }

    @Override
    public PhoneVerificationReservation reserve(
            String verificationId,
            PhoneVerificationPurpose purpose,
            String phoneHash,
            String codeHash,
            Instant requestedAt
    ) {
        String otpKey = otpKey(purpose, phoneHash);
        long windowNumber = requestedAt.getEpochSecond() / properties.rateWindow().toSeconds();
        String rateKey = "rate:phone-verification:" + phoneHash + ":" + windowNumber;

        Long result = redisTemplate.execute(
                requestScript,
                List.of(otpKey, rateKey),
                verificationId,
                codeHash,
                String.valueOf(requestedAt.getEpochSecond()),
                String.valueOf(properties.otpTtl().toSeconds()),
                String.valueOf(properties.resendDelay().toSeconds()),
                String.valueOf(properties.maxRequestsPerWindow()),
                String.valueOf(properties.rateWindow().toSeconds() + 10)
        );

        if (result == null) {
            throw new IllegalStateException("Redis에서 인증번호 요청 결과를 받지 못했습니다.");
        }
        if (result != 0) {
            throw new RateLimitExceededException(Math.abs(result));
        }

        return new PhoneVerificationReservation(requestedAt.plus(properties.otpTtl()));
    }

    @Override
    public void cancel(String verificationId, PhoneVerificationPurpose purpose, String phoneHash) {
        redisTemplate.execute(cancelScript, List.of(otpKey(purpose, phoneHash)), verificationId);
    }

    @Override
    public PhoneVerificationConfirmation confirm(
            String verificationId,
            PhoneVerificationPurpose purpose,
            String phoneHash,
            String codeHash,
            String verificationTokenHash,
            Instant confirmedAt
    ) {
        Long result = redisTemplate.execute(
                confirmScript,
                List.of(
                        otpKey(purpose, phoneHash),
                        "auth:verification:" + verificationTokenHash
                ),
                verificationId,
                codeHash,
                String.valueOf(properties.maxAttempts()),
                purpose.name(),
                phoneHash,
                String.valueOf(confirmedAt.getEpochSecond()),
                String.valueOf(properties.verificationTtl().toSeconds())
        );

        if (result == null) {
            throw new IllegalStateException("Redis에서 인증번호 확인 결과를 받지 못했습니다.");
        }
        if (result == -1) {
            throw new BusinessException(ErrorCode.VERIFICATION_EXPIRED);
        }
        if (result == -2) {
            throw new BusinessException(ErrorCode.ATTEMPTS_EXCEEDED);
        }
        if (result > 0) {
            throw new InvalidVerificationCodeException(result.intValue());
        }

        return new PhoneVerificationConfirmation(confirmedAt.plus(properties.verificationTtl()));
    }

    private String otpKey(PhoneVerificationPurpose purpose, String phoneHash) {
        return "auth:otp:" + purpose.name() + ":" + phoneHash;
    }
}
