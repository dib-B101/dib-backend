package com.b101.dib.auth.external.kakao;

public interface KakaoOAuthClient {

    KakaoProfile authenticate(String authorizationCode, String redirectUri);
}
