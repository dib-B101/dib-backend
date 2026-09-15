package com.b101.dib.member.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSettlementAccountRequest {
    @NotBlank
    private String phoneVerificationToken;
    @NotBlank
    private String bankName;
    @NotBlank
    @Pattern(regexp = "^[0-9-]{8,30}$")
    private String accountNumber;
    @NotBlank
    private String accountHolder;
}
