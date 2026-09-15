package com.b101.dib.auth.repository;

import java.time.Duration;

public interface PasswordResetTokenStore {

    void save(String tokenHash, Long memberId, Duration ttl);

    Long consume(String tokenHash);
}
