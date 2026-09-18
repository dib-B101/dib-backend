package com.b101.dib.auth.command.dto;

public record KakaoAuthResponse(
        boolean isNewMember,
        LoginMemberResponse member,
        String signupToken,
        KakaoProfileResponse kakaoProfile,
        String accessToken,
        String refreshToken,
        Long accessExpiresIn
) {
}
