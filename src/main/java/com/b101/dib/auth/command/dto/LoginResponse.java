package com.b101.dib.auth.command.dto;

public record LoginResponse(
        LoginMemberResponse member,
        String accessToken,
        String refreshToken,
        long accessExpiresIn
) {
}
