package com.b101.dib.auth.repository;

import java.time.Instant;

import com.b101.dib.auth.domain.AuthTokenPair;

public interface RefreshSessionStore {

    void save(Long memberId, String deviceId, AuthTokenPair tokens);

    RefreshSession findForRefresh(String presentedTokenHash, String deviceId, Instant now);

    void rotate(RefreshSession session, String presentedTokenHash, AuthTokenPair tokens);

    void revoke(Long memberId, String deviceId);
}
