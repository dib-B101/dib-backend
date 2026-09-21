package com.b101.dib.auth.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("auth.kakao")
public record KakaoProperties(
        String clientId,
        String clientSecret,
        String tokenUrl,
        String userInfoUrl,
        Duration signupTokenTtl,
        List<String> redirectUris
) {
}
