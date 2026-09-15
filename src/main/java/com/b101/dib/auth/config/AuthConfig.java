package com.b101.dib.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AuthConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DefaultRedisScript<Long> saveRefreshSessionScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/save-refresh-session.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> rotateRefreshSessionScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/rotate-refresh-session.lua"));
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> revokeRefreshSessionScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("redis/revoke-refresh-session.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
