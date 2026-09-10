package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.PhoneVerificationConfirmRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.domain.PhoneVerificationPurpose;

public interface PhoneVerificationService {

    PhoneVerificationResponse request(PhoneVerificationRequest request);

    PhoneVerificationConfirmResponse confirm(String verificationId, PhoneVerificationConfirmRequest request);

    void consumeVerificationToken(
            String verificationToken,
            PhoneVerificationPurpose purpose,
            String phoneNumber);
}
