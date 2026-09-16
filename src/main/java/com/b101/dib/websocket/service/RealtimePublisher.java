package com.b101.dib.websocket.service;

import com.b101.dib.websocket.dto.RealtimeMessage;
import com.b101.dib.websocket.dto.SocketEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

// 소켓 서비스는 SimpMessagingTemplate 대신 이걸 부른다.
// Redis Pub/Sub 로 모든 Pod 에 뿌리고, 각 Pod 의 RealtimeRedisSubscriber 가 자기 세션에 전달한다.
// Redis 가 꺼져 있거나(dib.realtime.redis-enabled=false) 발행이 실패하면 이 Pod 에만 직접 보낸다 — 입찰·주문은 소켓 때문에 실패하지 않는다
@Service
@Slf4j
public class RealtimePublisher {
    public static final String CHANNEL = "dib:realtime";

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final boolean redisEnabled;

    public RealtimePublisher(StringRedisTemplate redisTemplate, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper,
                             @Value("${dib.realtime.redis-enabled:true}") boolean redisEnabled) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.redisEnabled = redisEnabled;
    }

    public void broadcast(String destination, SocketEnvelope envelope) {
        publish(RealtimeMessage.topic(destination, envelope));
    }

    public void sendToUser(String user, String destination, SocketEnvelope envelope) {
        publish(RealtimeMessage.user(user, destination, envelope));
    }

    private void publish(RealtimeMessage message) {
        if (redisEnabled) {
            try {
                redisTemplate.convertAndSend(CHANNEL, objectMapper.writeValueAsString(message));
                return;
            } catch (Exception e) {
                log.warn("Redis 발행 실패 — 이 Pod 에만 직접 전송 destination={} : {}", message.getDestination(), e.getMessage());
            }
        }
        deliverLocal(message);
    }

    // 이 Pod 에 붙은 세션으로 실제 전송. Subscriber 도 이걸 부른다
    public void deliverLocal(RealtimeMessage message) {
        if (message.getUser() == null) {
            messagingTemplate.convertAndSend(message.getDestination(), message.getEnvelope());
        } else {
            messagingTemplate.convertAndSendToUser(message.getUser(), message.getDestination(), message.getEnvelope());
        }
    }
}
