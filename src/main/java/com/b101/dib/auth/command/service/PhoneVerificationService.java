package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.PhoneVerificationConfirmRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;

public interface PhoneVerificationService {

    PhoneVerificationResponse request(PhoneVerificationRequest request);

    PhoneVerificationConfirmResponse confirm(String verificationId, PhoneVerificationConfirmRequest request);
}
