package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.LogoutRequest;
import com.b101.dib.auth.command.dto.TokenRefreshRequest;
import com.b101.dib.auth.command.dto.TokenRefreshResponse;

public interface TokenSessionService {

    TokenRefreshResponse refresh(TokenRefreshRequest request);

    void logout(String authorizationHeader, LogoutRequest request);
}

