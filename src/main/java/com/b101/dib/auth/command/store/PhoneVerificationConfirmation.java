package com.b101.dib.auth.command.store;

import java.time.Instant;

public record PhoneVerificationConfirmation(
        Instant expiresAt
) {
}
