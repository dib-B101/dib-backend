package com.b101.dib.auth.command.controller;

import com.b101.dib.auth.command.dto.PhoneVerificationConfirmRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.command.dto.SignupRequest;
import com.b101.dib.auth.command.dto.SignupResponse;
import com.b101.dib.auth.command.service.PhoneVerificationService;
import com.b101.dib.auth.command.service.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final PhoneVerificationService phoneVerificationService;
    private final SignupService signupService;

    @PostMapping("/phone-verifications")
    public ResponseEntity<PhoneVerificationResponse> requestPhoneVerification(
            @RequestBody PhoneVerificationRequest request
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(phoneVerificationService.request(request));
    }

    @PostMapping("/phone-verifications/{verificationId}/confirm")
    public ResponseEntity<PhoneVerificationConfirmResponse> confirmPhoneVerification(
            @PathVariable String verificationId,
            @RequestBody PhoneVerificationConfirmRequest request
    ) {
        return ResponseEntity.ok(phoneVerificationService.confirm(verificationId, request));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signupService.signup(request));
    }
}
