package com.b101.dib.auth.command.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 비밀번호 재설정 링크 요청 DTO */
public record PasswordResetLinkRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 20) String phoneNumber,
        @NotBlank @Size(max = 512) String phoneVerificationToken
) {
}
