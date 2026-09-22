package com.b101.dib.auth.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 메일 발송 업체가 붙기 전까지의 기본 구현 — 어느 프로필에서나 뜬다.
// LoggingSmsSender 와 같은 이유: @Profile("local") 이면 prod 에 EmailSender 빈이 없어 PasswordResetServiceImpl 이 못 뜬다
@Slf4j
@Component
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendPasswordResetLink(String email, String resetLink) {
        log.info("비밀번호 재설정 메일 발송(로그만, 실제 전송 없음) - 수신자: {}, 링크: {}", mask(email), resetLink);
    }

    private String mask(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "*****";
        }
        return email.substring(0, 1) + "*****" + email.substring(atIndex);
    }
}
