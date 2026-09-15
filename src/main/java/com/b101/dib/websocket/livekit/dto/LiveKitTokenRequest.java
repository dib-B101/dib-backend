package com.b101.dib.websocket.livekit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LiveKitTokenRequest {

    @NotBlank
    private String roomName;

    @NotBlank
    private String participantName;

}