package com.b101.dib.live.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;

@Service
public class LiveKitService {

    private final String livekitUrl;
    private final String apiKey;
    private final String apiSecret;

    public LiveKitService(
            @Value("${livekit.url}") String livekitUrl,
            @Value("${livekit.api-key}") String apiKey,
            @Value("${livekit.api-secret}") String apiSecret
    ) {
        this.livekitUrl = livekitUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public String createToken(
            String roomName,
            String identity,
            boolean canPublish
    ) {

        AccessToken token = new AccessToken(
                apiKey,
                apiSecret
        );

        token.setIdentity(identity);

        token.addGrants(
                new RoomJoin(true),
                new RoomName(roomName),
                new CanPublish(canPublish),
                new CanSubscribe(true)
        );

        return token.toJwt();
    }

    public String getLivekitUrl() {
        return livekitUrl;
    }
}