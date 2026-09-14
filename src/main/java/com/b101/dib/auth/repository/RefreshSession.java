package com.b101.dib.auth.repository;

import java.time.Instant;

public record RefreshSession(
        Long memberId,
        String deviceId,
        String familyId,
        Instant absoluteExpiresAt
) {
}

