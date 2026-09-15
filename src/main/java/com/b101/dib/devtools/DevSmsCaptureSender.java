package com.b101.dib.devtools;

import com.b101.dib.auth.sms.SmsSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Primary
@Profile("local")
@Slf4j
public class DevSmsCaptureSender implements SmsSender {
    private static final Pattern CODE = Pattern.compile("(\\d{6})");
    private final Map<String, String> lastCodes = new ConcurrentHashMap<>();

    @Override
    public void send(String phoneNumber, String message) {
        String key = digits(phoneNumber);
        Matcher m = CODE.matcher(message);
        if (m.find()) {
            lastCodes.put(key, m.group(1));
        }
        log.info("[DEV] SMS 캡처 - 수신번호 {}, 내용: {}", key, message);
    }

    public String lastCode(String phoneNumber) {
        return lastCodes.get(digits(phoneNumber));
    }

    private static String digits(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}
