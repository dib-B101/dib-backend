package com.b101.dib.auth.command.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("auth.phone-verification")
public record PhoneVerificationProperties(
        Duration otpTtl, // 인증번호 유효 시간
        Duration verificationTtl, // 인증 완료 토큰 유효 시간
        Duration resendDelay, // 재요청 대기 시간
        Duration rateWindow, // 요청 횟수를 제한하는 시간 범위
        int maxRequestsPerWindow, // 제한 시간 내 허용되는 최대 요청 횟수
        int maxAttempts, // 인증번호 확인 최대 실패 횟수
        String hmacSecret // HMAC 해시 비밀키
) {
}
