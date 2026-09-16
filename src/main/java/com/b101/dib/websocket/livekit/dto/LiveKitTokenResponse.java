package com.b101.dib.websocket.livekit.dto;

public record LiveKitTokenResponse(
        String serverUrl,
        String token,
        String roomName,
        String participantName
) {
}