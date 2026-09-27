package com.b101.dib.auth.command.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.b101.dib.auth.command.dto.FirebasePhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.config.PhoneVerificationProperties;
import com.b101.dib.auth.domain.PhoneNumber;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class FirebasePhoneVerificationService {
    private static final Duration MAX_AUTH_AGE = Duration.ofMinutes(5);

    private final FirebasePhoneTokenVerifier tokenVerifier;
    private final StringRedisTemplate redisTemplate;
    private final PhoneVerificationProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final DefaultRedisScript<Long> issueScript;

    public FirebasePhoneVerificationService(
            FirebasePhoneTokenVerifier tokenVerifier,
            StringRedisTemplate redisTemplate,
            PhoneVerificationProperties properties,
            SecureRandom secureRandom,
            Clock clock,
            @Qualifier("issueFirebasePhoneVerificationScript") DefaultRedisScript<Long> issueScript
    ) {
        this.tokenVerifier = tokenVerifier;
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.secureRandom = secureRandom;
        this.clock = clock;
        this.issueScript = issueScript;
    }

    public PhoneVerificationConfirmResponse verify(FirebasePhoneVerificationRequest request) {
        if (request == null || request.purpose() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        String phone = PhoneNumber.from(request.phoneNumber()).value();
        FirebasePhoneTokenVerifier.VerifiedPhone verified = tokenVerifier.verify(request.idToken());
        Instant now = clock.instant();
        Duration age = Duration.between(verified.authenticatedAt(), now);
        if (age.isNegative() || age.compareTo(MAX_AUTH_AGE) > 0 || !toLocalPhone(verified.phoneNumber()).equals(phone)) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
        }
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        String verificationToken = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        String tokenHash = hmac("token:" + verificationToken);
        String replayHash = hmac("firebase:" + verified.uid() + ":" + verified.authenticatedAt().getEpochSecond()
                + ":" + request.purpose());
        Long result = redisTemplate.execute(issueScript,
                List.of("auth:firebase-used:" + replayHash, "auth:verification:" + tokenHash),
                request.purpose().name(), hmac("phone:" + phone),
                String.valueOf(now.getEpochSecond()), String.valueOf(properties.verificationTtl().toSeconds()),
                String.valueOf(MAX_AUTH_AGE.toSeconds()));
        if (result == null) throw new IllegalStateException("Firebase 인증 저장 결과를 받지 못했습니다.");
        if (result != 1) throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
        return new PhoneVerificationConfirmResponse(verificationToken,
                now.plus(properties.verificationTtl()).atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime());
    }

    private String toLocalPhone(String firebasePhone) {
        if (firebasePhone != null && firebasePhone.matches("\\+8210\\d{8}")) {
            return "0" + firebasePhone.substring(3);
        }
        throw new BusinessException(ErrorCode.INVALID_VERIFICATION);
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.hmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("휴대전화 인증 해시를 생성할 수 없습니다.", e);
        }
    }
}
