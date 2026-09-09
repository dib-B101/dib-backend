package com.b101.dib.auth.command.dto;

public record PhoneVerificationRequest(
        String phoneNumber, // 인증번호를 요청할 전화번호
        PhoneVerificationPurpose purpose // 인증번호 요청 목적
) {
}
