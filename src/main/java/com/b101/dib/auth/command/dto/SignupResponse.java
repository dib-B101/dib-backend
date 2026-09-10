package com.b101.dib.auth.command.dto;

import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;

public record SignupResponse(
        Long memberId,
        String email,
        String nickname,
        MemberStatus status,
        MemberRole role,
        String accessToken,
        String refreshToken
) {
}
