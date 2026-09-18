package com.b101.dib.auth.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KakaoAuthRequest(
        @NotBlank @Size(max = 512) String authorizationCode,
        @NotBlank @Size(max = 500) String redirectUri,
        @Size(max = 512) String phoneVerificationToken,
        @NotBlank @Size(max = 128) String deviceId
) {
}
