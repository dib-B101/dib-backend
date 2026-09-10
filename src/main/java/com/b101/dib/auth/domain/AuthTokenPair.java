package com.b101.dib.auth.domain;

import java.time.Instant;

public record AuthTokenPair(
        String accessToken, // 액세스 토큰
        String refreshToken, // 리프레시 토큰
        String refreshTokenHash, // 리프레시 토큰 해시
        String familyId, // 토큰 패밀리 ID
        Instant issuedAt, // 토큰 발급 시각
        Instant absoluteExpiresAt // 토큰 절대 만료 시각
) {
}
