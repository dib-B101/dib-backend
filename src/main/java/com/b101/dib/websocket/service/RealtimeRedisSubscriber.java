package com.b101.dib.websocket.service;

import com.b101.dib.websocket.dto.RealtimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

// dib:realtime 구독. 어느 Pod 가 발행했든 여기서 받아 자기 Pod 세션에 전달한다 (세션이 없는 Pod 은 보낼 곳이 없어 조용히 끝난다)
@Component
@RequiredArgsConstructor
@Slf4j
public class RealtimeRedisSubscriber implements MessageListener {
    private final RealtimePublisher realtimePublisher;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            RealtimeMessage m = objectMapper.readValue(new String(message.getBody(), StandardCharsets.UTF_8), RealtimeMessage.class);
            realtimePublisher.deliverLocal(m);
        } catch (Exception e) {
            log.warn("realtime 메시지 처리 실패: {}", e.getMessage());
        }
    }
}
