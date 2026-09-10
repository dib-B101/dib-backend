package com.b101.dib.auth.repository;

import com.b101.dib.auth.domain.AuthTokenPair;

public interface RefreshSessionStore {

    void save(Long memberId, String deviceId, AuthTokenPair tokens);
}
