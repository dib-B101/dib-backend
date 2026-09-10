package com.b101.dib.auth.token;

import com.b101.dib.auth.domain.AuthTokenPair;
import com.b101.dib.member.domain.Member;

public interface TokenIssuer {

    AuthTokenPair issue(Member member);
}
