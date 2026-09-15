package com.b101.dib.websocket.livekit.service;

import io.livekit.server.AccessToken;
import io.livekit.server.CanPublish;
import io.livekit.server.CanPublishData;
import io.livekit.server.CanSubscribe;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastRole;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.websocket.livekit.config.LiveKitConfig;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenRequest;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;

@Service
@RequiredArgsConstructor
public class LiveKitService {
    
	private final LiveKitConfig liveKitConfig;
	private final LiveBroadcastRepository liveBroadcastRepository;
	
	public LiveKitTokenResponse issueToken(Long myId, Long liveBroadcastId) {
		LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(liveBroadcastId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_FOUND));
		
		LiveBroadcastRole role = liveBroadcast.getMemberId().equals(myId) 
                ? LiveBroadcastRole.HOST 
                : LiveBroadcastRole.VIEWER;
		
		String roomName = liveBroadcast.getLivekitRoomName();
		
		String participantName = "user-" + myId; 
        String token = createToken(
                roomName,
                participantName,
                role
        );
		
		return new LiveKitTokenResponse(
                liveKitConfig.getUrl(),
                token,
                roomName,
                participantName
        );
	}

    private String createToken(
            String roomName,
            String participantName,
            LiveBroadcastRole role
    ) {
        AccessToken accessToken = new AccessToken(
                liveKitConfig.getApiKey(),
                liveKitConfig.getApiSecret()
        );

        accessToken.setIdentity(participantName);
        
        if (role == LiveBroadcastRole.HOST) {
            accessToken.addGrants(
                    new RoomJoin(true),
                    new RoomName(roomName),
                    new CanPublish(true),
                    new CanSubscribe(true),
                    new CanPublishData(true)
            );
        } else {
        	// VIEWER(시청자)
            accessToken.addGrants(
                    new RoomJoin(true),
                    new RoomName(roomName),
                    new CanPublish(false),
                    new CanSubscribe(true),
                    new CanPublishData(true)
            );
        }
        return accessToken.toJwt();
    }

}