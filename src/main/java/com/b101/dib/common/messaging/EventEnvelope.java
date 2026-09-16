package com.b101.dib.common.messaging;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

// Kafka 메시지 봉투. 소켓 SocketEnvelope 와 같은 모양 + aggregate 정보. Consumer 는 payload 를 Map 으로 읽는다
@Getter
@Setter
@NoArgsConstructor
public class EventEnvelope {
    private String eventType;
    private String eventId;
    private String occurredAt;
    private String aggregateType;
    private Long aggregateId;
    private Map<String, Object> payload = new HashMap<>();

    public Long getLong(String key) {
        Object v = payload.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        return Long.valueOf(v.toString());
    }

    public Integer getInt(String key) {
        Long v = getLong(key);
        return v == null ? null : v.intValue();
    }

    public String getString(String key) {
        Object v = payload.get(key);
        return v == null ? null : v.toString();
    }

    public boolean getBoolean(String key) {
        Object v = payload.get(key);
        return v != null && Boolean.parseBoolean(v.toString());
    }
}
