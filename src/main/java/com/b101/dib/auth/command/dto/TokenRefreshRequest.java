package com.b101.dib.auth.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TokenRefreshRequest(
        @NotBlank @Size(max = 512) String refreshToken,
        @NotBlank @Size(max = 128) String deviceId
) {
}

