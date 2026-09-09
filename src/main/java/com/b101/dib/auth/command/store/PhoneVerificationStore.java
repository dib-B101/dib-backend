package com.b101.dib.auth.command.store;

import java.time.Instant;

import com.b101.dib.auth.command.dto.PhoneVerificationPurpose;

public interface PhoneVerificationStore {

    // 인증 요청 정보를 Redis에 저장하고 인증 요청을 검증한다.
    PhoneVerificationReservation reserve(
            String verificationId,
            PhoneVerificationPurpose purpose,
            String phoneHash,
            String codeHash,
            Instant requestedAt);

    // 인증 요청 정보를 Redis에서 삭제한다.
    void cancel(String verificationId, PhoneVerificationPurpose purpose, String phoneHash);
}
