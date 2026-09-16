package com.b101.dib.common.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class SchedulingConfig {

    @Bean
    public LockProvider lockProvider(
            RedisConnectionFactory connectionFactory,
            @Value("${spring.application.name}") String applicationName,
            Environment environment
    ) {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            profiles = environment.getDefaultProfiles();
        }
        String lockEnvironment = applicationName + ":" + String.join(",", profiles);
        return new RedisLockProvider(connectionFactory, lockEnvironment);
    }
}
