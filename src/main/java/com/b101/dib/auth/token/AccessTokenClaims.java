package com.b101.dib.auth.token;

import com.b101.dib.member.domain.MemberRole;

public record AccessTokenClaims(
        Long memberId,
        MemberRole role
) {
}

