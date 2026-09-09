package com.b101.dib.auth.query.service;

import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;

public interface AuthQueryService {

    EmailAvailabilityResponse checkEmailAvailability(String email);
}
