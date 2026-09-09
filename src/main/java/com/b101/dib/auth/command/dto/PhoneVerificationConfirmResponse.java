package com.b101.dib.auth.command.dto;

import java.time.OffsetDateTime;

public record PhoneVerificationConfirmResponse(
        String verificationToken,
        OffsetDateTime expiresAt
) {
}
