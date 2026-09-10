package com.b101.dib.auth.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
public record JwtProperties(
        String secret, // JWT 서명에 사용되는 비밀 키
        long accessTokenValiditySeconds, // 액세스 토큰의 유효 기간
        Duration refreshTokenInactivity, // 리프레시 토큰의 만료 기간
        Duration refreshTokenAbsolute // 리프레시 토큰의 절대 만료 기간
) {
}
