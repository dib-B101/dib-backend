package com.b101.dib.auth.token;

import java.time.Instant;

import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.member.domain.Member;

public interface TokenIssuer {

    AuthTokenPair issue(Member member);

    AuthTokenPair rotate(Member member, String familyId, Instant absoluteExpiresAt);
}
