package com.b101.dib.auth.command.service;

import java.time.Instant;

public interface FirebasePhoneTokenVerifier {
    VerifiedPhone verify(String idToken);

    record VerifiedPhone(String uid, String phoneNumber, Instant authenticatedAt) {
    }
}
