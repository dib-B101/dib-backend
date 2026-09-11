package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.LoginRequest;
import com.b101.dib.auth.command.dto.LoginResponse;

public interface LoginService {

    LoginResponse login(LoginRequest request);
}
