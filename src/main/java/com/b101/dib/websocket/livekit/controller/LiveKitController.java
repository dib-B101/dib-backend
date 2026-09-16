package com.b101.dib.websocket.livekit.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.websocket.livekit.dto.LiveKitTokenRequest;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;
import com.b101.dib.websocket.livekit.service.LiveKitService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/livekit")
@RequiredArgsConstructor
public class LiveKitController {

    private final LiveKitService liveKitService;

//    @PostMapping("/token")
//    public LiveKitTokenResponse createToken(
//            @Valid @RequestBody LiveKitTokenRequest request
//    ) {
//        return liveKitService.createToken(request);
//    }
}