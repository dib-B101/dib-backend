package com.b101.dib.auth.external.kakao;

import java.time.LocalDate;

import com.b101.dib.member.domain.Gender;

public record KakaoProfile(
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl,
        String name,
        Gender gender,
        LocalDate birthDate,
        String phoneNumber
) {
}
