package com.b101.dib.auth.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 비밀번호 재설정 관련 설정값 */
@ConfigurationProperties("auth.password-reset")
public record PasswordResetProperties(
                Duration tokenTtl,
                String resetPageUrl) {
}
