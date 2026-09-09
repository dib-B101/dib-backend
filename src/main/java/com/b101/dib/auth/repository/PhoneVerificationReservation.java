package com.b101.dib.auth.repository;

import java.time.Instant;

public record PhoneVerificationReservation(Instant expiresAt) {
}
