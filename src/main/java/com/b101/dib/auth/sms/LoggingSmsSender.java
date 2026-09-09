package com.b101.dib.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class LoggingSmsSender implements SmsSender {

    @Override
    public void send(String phoneNumber, String message) {
        log.info("로컬 SMS 발송 - 수신번호: {}, 내용: {}", mask(phoneNumber), message);
    }

    private String mask(String phoneNumber) {
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }
}
