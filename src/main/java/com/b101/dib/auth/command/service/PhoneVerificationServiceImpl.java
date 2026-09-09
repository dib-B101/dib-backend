package com.b101.dib.auth.command.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.b101.dib.auth.command.config.PhoneVerificationProperties;
import com.b101.dib.auth.command.dto.PhoneVerificationPurpose;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.command.sms.SmsSender;
import com.b101.dib.auth.command.store.PhoneVerificationReservation;
import com.b101.dib.auth.command.store.PhoneVerificationStore;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private final PhoneVerificationStore phoneVerificationStore;
    private final SmsSender smsSender;
    private final PhoneVerificationProperties properties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Override
    public PhoneVerificationResponse request(PhoneVerificationRequest request) {
        String phoneNumber = normalizePhoneNumber(request == null ? null : request.phoneNumber());
        PhoneVerificationPurpose purpose = requirePurpose(request == null ? null : request.purpose());
        String code = "%06d".formatted(secureRandom.nextInt(1_000_000));
        String phoneHash = hmac("phone:" + phoneNumber);
        String verificationId = createVerificationId(purpose, phoneHash);
        String codeHash = hmac("otp:" + verificationId + ":" + code);
        Instant now = clock.instant();

        PhoneVerificationReservation reservation = phoneVerificationStore.reserve(
                verificationId, purpose, phoneHash, codeHash, now);

        try {
            smsSender.send(phoneNumber, "[DIB] 인증번호는 " + code + "입니다. 3분 이내에 입력해주세요.");
        } catch (RuntimeException e) {
            phoneVerificationStore.cancel(verificationId, purpose, phoneHash);
            throw e;
        }

        return new PhoneVerificationResponse(
                verificationId,
                reservation.expiresAt().atZone(KOREA_ZONE).toOffsetDateTime(),
                properties.resendDelay().toSeconds());
    }

    // 휴대전화 번호 정규화 및 유효성 검사
    private String normalizePhoneNumber(String rawPhoneNumber) {
        if (rawPhoneNumber == null) {
            throw new BusinessException(ErrorCode.INVALID_PHONE);
        }

        String normalized = rawPhoneNumber.trim().replaceAll("[\\s-]", "");
        if (!normalized.matches("010\\d{8}")) {
            throw new BusinessException(ErrorCode.INVALID_PHONE);
        }
        return normalized;
    }

    // 인증 목적 유효성 검사
    private PhoneVerificationPurpose requirePurpose(PhoneVerificationPurpose purpose) {
        if (purpose == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return purpose;
    }

    // 휴대전화 인증 요청 ID 생성 
    private String createVerificationId(PhoneVerificationPurpose purpose, String phoneHash) {
        String payload = purpose.name() + ":" + phoneHash + ":" + UUID.randomUUID();
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)); // BASE64 URL-safe 인코딩
        return encodedPayload + "." + hmac("verification:" + encodedPayload); // base(정보).hmac(base(정보))
    }

    // HMAC-SHA256 해시 생성
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
