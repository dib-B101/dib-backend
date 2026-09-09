package com.b101.dib.auth.command.controller;

import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.command.service.PhoneVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final PhoneVerificationService phoneVerificationService;

    @PostMapping("/phone-verifications")
    public ResponseEntity<PhoneVerificationResponse> requestPhoneVerification(
            @RequestBody PhoneVerificationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(phoneVerificationService.request(request));
    }
}
