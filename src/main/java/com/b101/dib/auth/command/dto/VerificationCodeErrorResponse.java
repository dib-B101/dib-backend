package com.b101.dib.auth.command.dto;

public record VerificationCodeErrorResponse(
        String code,
        String message,
        int remainingAttempts
) {
}
