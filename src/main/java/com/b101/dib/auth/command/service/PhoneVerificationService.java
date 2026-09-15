package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.PhoneVerificationConfirmRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;

public interface PhoneVerificationService {

    /** 전화번호 인증을 요청한다. */
    PhoneVerificationResponse request(PhoneVerificationRequest request);

    /** 전화번호 인증을 확인한다. */
    PhoneVerificationConfirmResponse confirm(String verificationId, PhoneVerificationConfirmRequest request);

    /** 전화번호 인증 토큰을 소모한다. */
    void consumeVerificationToken(
            String verificationToken,
            PhoneVerificationPurpose purpose,
            String phoneNumber);
}
