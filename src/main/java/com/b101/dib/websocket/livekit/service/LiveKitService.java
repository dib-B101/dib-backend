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
		// 키가 비면 아래 new AccessToken(null, null) 이 SDK 예외로 터져 500 이 난다.
		// 배포 프로필에 livekit 블록이 없던 시절에 실제로 그랬다. 여기서 503 으로 막아 앱이 원인을 보여주게 한다
		if (isBlank(liveKitConfig.getUrl()) || isBlank(liveKitConfig.getApiKey()) || isBlank(liveKitConfig.getApiSecret())) {
			throw new BusinessException(ErrorCode.STREAM_UNAVAILABLE);
		}
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

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

}