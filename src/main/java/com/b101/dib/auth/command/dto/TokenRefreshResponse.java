package com.b101.dib.auth.command.dto;

public record TokenRefreshResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresIn
) {
}

