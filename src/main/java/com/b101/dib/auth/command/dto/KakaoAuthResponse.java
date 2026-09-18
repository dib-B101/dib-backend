package com.b101.dib.auth.command.dto;

public record KakaoAuthResponse(
        boolean isNewMember,
        LoginMemberResponse member,
        String accessToken,
        String refreshToken,
        long accessExpiresIn
) {
}
