package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;

public interface PhoneVerificationService {

    PhoneVerificationResponse request(PhoneVerificationRequest request);
}
