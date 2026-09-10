package com.b101.dib.auth.command.dto;

import java.time.LocalDate;

import com.b101.dib.auth.command.validation.ValidPassword;
import com.b101.dib.member.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @ValidPassword String password,
        @NotBlank @Size(max = 10) String name,
        @NotBlank @Size(max = 50) String nickname,
        @NotNull Gender gender,
        @NotNull LocalDate birthDate,
        @NotBlank String phoneNumber,
        @NotBlank String phoneVerificationToken,
        @NotBlank @Size(max = 128) String deviceId) {
}
