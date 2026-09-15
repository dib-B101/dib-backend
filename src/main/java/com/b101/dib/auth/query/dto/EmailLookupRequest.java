package com.b101.dib.auth.query.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailLookupRequest(
        @NotBlank(message = "본인인증 토큰은 필수입니다.")
        @Size(max = 512, message = "본인인증 토큰이 너무 깁니다.")
        String verificationToken,

        @NotBlank(message = "휴대전화번호는 필수입니다.")
        @Size(max = 20, message = "휴대전화번호가 너무 깁니다.")
        String phoneNumber
) {
}
