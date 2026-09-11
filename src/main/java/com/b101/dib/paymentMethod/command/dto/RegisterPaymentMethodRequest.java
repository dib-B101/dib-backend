package com.b101.dib.paymentMethod.command.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterPaymentMethodRequest(@NotBlank String authKey, @NotBlank String customerKey) {
}
