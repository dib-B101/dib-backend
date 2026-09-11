package com.b101.dib.devtools;

import jakarta.validation.constraints.NotBlank;

public record DevCardRegisterRequest(@NotBlank String cardNumber,
                                     @NotBlank String cardExpirationYear,
                                     @NotBlank String cardExpirationMonth,
                                     @NotBlank String customerIdentityNumber) {
}
