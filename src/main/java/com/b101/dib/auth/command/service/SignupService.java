package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.SignupRequest;
import com.b101.dib.auth.command.dto.SignupResponse;

public interface SignupService {

    SignupResponse signup(SignupRequest request);
}
