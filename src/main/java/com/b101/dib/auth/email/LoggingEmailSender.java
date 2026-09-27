package com.b101.dib.auth.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "auth.mail.mode", havingValue = "logging", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    @Override
    public void sendPasswordResetLink(String email, String resetLink) {
        log.info("로컬 메일 발송 생략 - 수신자: {}", mask(email));
    }

    private String mask(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "*****";
        }
        return email.substring(0, 1) + "*****" + email.substring(atIndex);
    }
}
