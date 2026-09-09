package com.b101.dib.auth.config;

import java.security.SecureRandom;
import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
@EnableConfigurationProperties(PhoneVerificationProperties.class)
public class PhoneVerificationConfig {

    @Bean
    public Clock clock() {
        // 현재 시간
        return Clock.systemUTC();
    }

    @Bean
    public SecureRandom secureRandom() {
        // 인증번호 생성을 위한 난수 생성기
        return new SecureRandom();
    }

    @Bean
    public DefaultRedisScript<Long> requestPhoneVerificationScript() {
        // 인증 요청 시 실행할 Redis Lua 스크립트 등록
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/request-phone-verification.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> cancelPhoneVerificationScript() {
        // 인증 취소 시 실행할 Redis Lua 스크립트 등록
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/cancel-phone-verification.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> confirmPhoneVerificationScript() {
        // 인증 확인 시 실행할 Redis Lua 스크립트 등록
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/confirm-phone-verification.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
