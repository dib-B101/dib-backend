package com.b101.dib.auth.repository;

import java.time.Instant;

import com.b101.dib.auth.domain.PhoneVerificationPurpose;

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

    // 인증 요청 정보를 Redis에서 조회하고 인증 요청을 검증한다.
    PhoneVerificationConfirmation confirm(
            String verificationId,
            PhoneVerificationPurpose purpose,
            String phoneHash,
            String codeHash,
            String verificationTokenHash,
            Instant confirmedAt);

    void consume(String verificationTokenHash, PhoneVerificationPurpose purpose, String phoneHash);
}
