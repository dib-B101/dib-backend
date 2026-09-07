package com.b101.dib.live.controller;

import com.b101.dib.live.dto.LiveKitTokenResponse;
import com.b101.dib.live.service.LiveKitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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