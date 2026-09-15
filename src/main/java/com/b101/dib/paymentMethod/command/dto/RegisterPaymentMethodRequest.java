package com.b101.dib.paymentMethod.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterPaymentMethodRequest {
    @NotBlank
    private String authKey;
    @NotBlank
    private String customerKey;
}
