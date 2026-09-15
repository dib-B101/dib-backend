package com.b101.dib.liveBroadcast.command.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.b101.dib.liveBroadcast.command.dto.LiveKitTokenResponse;
import com.b101.dib.liveBroadcast.command.service.LiveKitService;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveKitController {

    private final LiveKitService liveKitService;

    @PostMapping("/token")
    public LiveKitTokenResponse createToken(
            @RequestParam String roomName,
            @RequestParam String identity
    ) {

        String token = liveKitService.createToken(
                roomName,
                identity,
                true
        );

        return new LiveKitTokenResponse(
                liveKitService.getLivekitUrl(),
                token,
                roomName
        );
    }
}