package com.b101.dib.auth.command.dto;

import com.b101.dib.auth.domain.PhoneVerificationPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FirebasePhoneVerificationRequest(
        @NotBlank String idToken,
        @NotBlank String phoneNumber,
        @NotNull PhoneVerificationPurpose purpose
) {
}
