package com.b101.dib.auth.external.kakao;

public record KakaoProfile(
        String providerUserId,
        String nickname,
        String profileImageUrl
) {
}
