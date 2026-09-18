package com.b101.dib.auth.repository;

public record KakaoSignupSession(
        String providerUserId,
        String nickname,
        String profileImageUrl
) {
}
