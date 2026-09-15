package com.b101.dib.auth.command.dto;

import com.b101.dib.auth.command.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 비밀번호 재설정 요청 DTO */
public record PasswordResetRequest(
        @NotBlank @Size(max = 512) String resetToken,
        @NotBlank @ValidPassword String newPassword
) {
}
