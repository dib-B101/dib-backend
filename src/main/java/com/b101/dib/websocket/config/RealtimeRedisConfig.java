package com.b101.dib.websocket.config;

import com.b101.dib.websocket.service.RealtimePublisher;
import com.b101.dib.websocket.service.RealtimeRedisSubscriber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

// Redis Pub/Sub 수신 컨테이너. dib.realtime.redis-enabled=false 면 만들지 않는다 (Redis 없는 로컬)
@Configuration
@ConditionalOnProperty(name = "dib.realtime.redis-enabled", havingValue = "true", matchIfMissing = true)
public class RealtimeRedisConfig {
    @Bean
    public RedisMessageListenerContainer realtimeListenerContainer(RedisConnectionFactory connectionFactory,
                                                                   RealtimeRedisSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(RealtimePublisher.CHANNEL));
        return container;
    }
}
