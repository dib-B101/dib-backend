package com.b101.dib.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 프론트 SocketEnvelope 와 동일 필드. 전송(STOMP) 위에 얹는 공통 봉투
@Getter
@Setter
@NoArgsConstructor
public class SocketEnvelope {
    private String eventType;
    private String eventId;
    private String commandId;
    private String occurredAt;
    private Map<String, Object> payload = new HashMap<>();

    public static SocketEnvelope of(String eventType, String commandId, Map<String, Object> payload) {
        SocketEnvelope e = new SocketEnvelope();
        e.setEventType(eventType);
        e.setEventId(UUID.randomUUID().toString());
        e.setCommandId(commandId);
        e.setOccurredAt(Instant.now().toString());
        e.setPayload(payload);
        return e;
    }
}
