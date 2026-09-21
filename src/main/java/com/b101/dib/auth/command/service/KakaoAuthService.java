package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;
import com.b101.dib.auth.command.dto.KakaoSignupRequest;

public interface KakaoAuthService {

    KakaoAuthResponse authenticate(KakaoAuthRequest request);

    KakaoAuthResponse signup(KakaoSignupRequest request);
}
