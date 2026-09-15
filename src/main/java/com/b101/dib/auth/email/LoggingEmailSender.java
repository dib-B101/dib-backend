package com.b101.dib.auth.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendPasswordResetLink(String email, String resetLink) {
        log.info("로컬 비밀번호 재설정 메일 발송 - 수신자: {}, 링크: {}", mask(email), resetLink);
    }

    private String mask(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "*****";
        }
        return email.substring(0, 1) + "*****" + email.substring(atIndex);
    }
}
