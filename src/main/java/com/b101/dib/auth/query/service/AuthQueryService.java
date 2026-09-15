package com.b101.dib.auth.query.service;

import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.dto.EmailLookupResponse;

public interface AuthQueryService {

    EmailAvailabilityResponse checkEmailAvailability(String email);

    EmailLookupResponse findEmail(String verificationToken, String phoneNumber);
}
