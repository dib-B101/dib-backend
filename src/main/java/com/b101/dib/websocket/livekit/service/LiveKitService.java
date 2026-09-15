package com.b101.dib.websocket.livekit.service;

import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.b101.dib.websocket.livekit.config.LiveKitConfig;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenRequest;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;

@Service
@RequiredArgsConstructor
public class LiveKitService {

    private final LiveKitConfig liveKitConfig;

    public LiveKitTokenResponse createToken(
            LiveKitTokenRequest request
    ) {
        AccessToken accessToken = new AccessToken(
                liveKitConfig.getApiKey(),
                liveKitConfig.getApiSecret()
        );

        accessToken.setIdentity(request.getParticipantName());
        
        accessToken.addGrants(
                new RoomJoin(true),
                new RoomName(request.getRoomName()),
                new CanPublish(true),
                new CanSubscribe(true)
        );

        

        String token = accessToken.toJwt();

        return new LiveKitTokenResponse(
                liveKitConfig.getUrl(),
                token,
                request.getRoomName(),
                request.getParticipantName()
        );
    }
}