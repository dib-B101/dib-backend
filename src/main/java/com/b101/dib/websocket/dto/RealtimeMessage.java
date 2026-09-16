package com.b101.dib.websocket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Redis 채널 dib:realtime 로 Pod 사이에 오가는 메시지. user 가 null 이면 /topic 브로드캐스트, 있으면 /user/{user}{destination}
@Getter
@Setter
@NoArgsConstructor
public class RealtimeMessage {
    private String destination;
    private String user;
    private SocketEnvelope envelope;

    public static RealtimeMessage topic(String destination, SocketEnvelope envelope) {
        RealtimeMessage m = new RealtimeMessage();
        m.setDestination(destination);
        m.setEnvelope(envelope);
        return m;
    }

    public static RealtimeMessage user(String user, String destination, SocketEnvelope envelope) {
        RealtimeMessage m = topic(destination, envelope);
        m.setUser(user);
        return m;
    }
}
