package com.b101.dib.common.lock;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 분산락 전용 Redisson 클라이언트. 기존 Lettuce(StringRedisTemplate·auth 세션)는 그대로 두고 락만 Redisson 으로.
// dib.lock.redis-enabled=false 면 빈을 만들지 않고 AuctionLock 이 DB 행 락만으로 동작한다
@Configuration
@ConditionalOnProperty(name = "dib.lock.redis-enabled", havingValue = "true", matchIfMissing = true)
public class RedisLockConfig {
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(@Value("${spring.data.redis.host:localhost}") String host,
                                         @Value("${spring.data.redis.port:6379}") int port,
                                         @Value("${spring.data.redis.password:}") String password) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password == null || password.isBlank() ? null : password)
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(8);
        return Redisson.create(config);
    }
}
