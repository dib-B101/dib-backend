package com.b101.dib.auth.command.service;

import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;

public interface KakaoAuthService {

    KakaoAuthResponse authenticate(KakaoAuthRequest request);
}
