package com.b101.dib.auth.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// 실제 문자 발송 업체가 붙기 전까지의 기본 구현 — 어느 프로필에서나 뜬다.
// 예전엔 @Profile("local") 이어서 prod 에는 SmsSender 빈이 하나도 없었고, PhoneVerificationServiceImpl 주입에
// 실패해 컨테이너가 기동 중에 죽었다(CrashLoopBackOff). local 에서는 DevSmsCaptureSender 가 @Primary 로 앞선다.
// 발송 업체 구현체를 만들면 그쪽에 @Primary 를 달거나 이 클래스를 @ConditionalOnMissingBean 으로 바꾼다
@Slf4j
@Component
public class LoggingSmsSender implements SmsSender {

    @Override
    public void send(String phoneNumber, String message) {
        log.info("SMS 발송(로그만, 실제 전송 없음) - 수신번호: {}, 내용: {}", mask(phoneNumber), message);
    }

    private String mask(String phoneNumber) {
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }
}
