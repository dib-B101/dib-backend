package com.b101.dib.auth.repository;

import java.time.Duration;

public interface KakaoSignupTokenStore {

    void save(String tokenHash, KakaoSignupSession session, Duration ttl);

    KakaoSignupSession find(String tokenHash);

    KakaoSignupSession consume(String tokenHash);
}
