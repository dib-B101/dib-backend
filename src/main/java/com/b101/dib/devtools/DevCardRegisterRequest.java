package com.b101.dib.devtools;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DevCardRegisterRequest {
    @NotBlank
    private String cardNumber;
    @NotBlank
    private String cardExpirationYear;
    @NotBlank
    private String cardExpirationMonth;
    @NotBlank
    private String customerIdentityNumber;
}
