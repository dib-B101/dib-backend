package com.b101.dib.auth.command.service;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.b101.dib.auth.config.PhoneVerificationProperties;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import com.b101.dib.auth.repository.PhoneVerificationConfirmation;
import com.b101.dib.auth.repository.PhoneVerificationReservation;
import com.b101.dib.auth.repository.PhoneVerificationStore;
import com.b101.dib.auth.sms.SmsSender;
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

    @Override
    public PhoneVerificationConfirmResponse confirm(
            String verificationId,
            PhoneVerificationConfirmRequest request
    ) {
        VerificationRequestIdentity identity = parseVerificationId(verificationId); // {purpose, phoneHash}
        String code = request == null || request.code() == null ? "" : request.code();
        String codeHash = hmac("otp:" + verificationId + ":" + code);
        String verificationToken = createVerificationToken();
        String verificationTokenHash = hmac("token:" + verificationToken);

        // 인증 토큰을 Redis에 저장한다.
        PhoneVerificationConfirmation confirmation = phoneVerificationStore.confirm(
                verificationId,
                identity.purpose(),
                identity.phoneHash(),
                codeHash,
                verificationTokenHash,
                clock.instant()
        );

        return new PhoneVerificationConfirmResponse(
                verificationToken,
                confirmation.expiresAt().atZone(KOREA_ZONE).toOffsetDateTime()
        );
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
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)); // Base64 URL-safe 인코딩
        return encodedPayload + "." + hmac("verification:" + encodedPayload); // base64(정보).hmac(base64(정보))
    }

    // 휴대전화 인증 요청 ID 파싱 및 검증
    private VerificationRequestIdentity parseVerificationId(String verificationId) {
        try {
            String[] tokenParts = verificationId.split("\\.", -1);

            // verificationId 디코딩 및 검증
            if (tokenParts.length != 2 || !MessageDigest.isEqual(
                    hmac("verification:" + tokenParts[0]).getBytes(StandardCharsets.UTF_8),
                    tokenParts[1].getBytes(StandardCharsets.UTF_8)
            )) {
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_ID);
            }

            // payload 디코딩 및 검증
            String payload = new String(
                    Base64.getUrlDecoder().decode(tokenParts[0]),
                    StandardCharsets.UTF_8
            );
            String[] payloadParts = payload.split(":", 3);
            if (payloadParts.length != 3 || payloadParts[1].isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_VERIFICATION_ID);
            }

            PhoneVerificationPurpose purpose = PhoneVerificationPurpose.valueOf(payloadParts[0]);
            UUID.fromString(payloadParts[2]); // UUID 형식 검증
            return new VerificationRequestIdentity(purpose, payloadParts[1]);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new BusinessException(ErrorCode.INVALID_VERIFICATION_ID);
        }
    }

    // 인증 토큰 생성
    private String createVerificationToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // HMAC-SHA256 값 생성
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

    private record VerificationRequestIdentity(
            PhoneVerificationPurpose purpose,
            String phoneHash
    ) {
    }
}
