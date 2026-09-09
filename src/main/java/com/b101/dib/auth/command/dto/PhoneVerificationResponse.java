package com.b101.dib.auth.command.dto;

import java.time.OffsetDateTime;

public record PhoneVerificationResponse(
        String verificationId, // 인증번호 요청 id
        OffsetDateTime expiresAt, // 인증번호 만료 시간
        long retryAfterSeconds // 인증번호 재요청 대기 시간
) {
}
