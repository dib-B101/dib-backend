package com.b101.dib.auth.command.controller;

import com.b101.dib.auth.command.dto.LoginRequest;
import com.b101.dib.auth.command.dto.LoginResponse;
import com.b101.dib.auth.command.dto.KakaoAuthRequest;
import com.b101.dib.auth.command.dto.KakaoAuthResponse;
import com.b101.dib.auth.command.dto.KakaoSignupRequest;
import com.b101.dib.auth.command.dto.LogoutRequest;
import com.b101.dib.auth.command.dto.PasswordResetLinkRequest;
import com.b101.dib.auth.command.dto.PasswordResetRequest;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.SignupRequest;
import com.b101.dib.auth.command.dto.SignupResponse;
import com.b101.dib.auth.command.dto.TokenRefreshRequest;
import com.b101.dib.auth.command.dto.TokenRefreshResponse;
import com.b101.dib.auth.command.service.LoginService;
import com.b101.dib.auth.command.service.KakaoAuthService;
import com.b101.dib.auth.command.service.PasswordResetService;
import com.b101.dib.auth.command.service.FirebasePhoneVerificationService;
import com.b101.dib.auth.command.dto.FirebasePhoneVerificationRequest;
import com.b101.dib.auth.command.service.SignupService;
import com.b101.dib.auth.command.service.TokenSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthCommandController {

    private final FirebasePhoneVerificationService firebasePhoneVerificationService;
    private final SignupService signupService;
    private final LoginService loginService;
    private final KakaoAuthService kakaoAuthService;
    private final TokenSessionService tokenSessionService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/phone-verifications/firebase")
    public ResponseEntity<PhoneVerificationConfirmResponse> verifyFirebasePhone(
            @Valid @RequestBody FirebasePhoneVerificationRequest request
    ) {
        return ResponseEntity.ok(firebasePhoneVerificationService.verify(request));
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(signupService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @PostMapping("/oauth/kakao")
    public ResponseEntity<KakaoAuthResponse> authenticateWithKakao(
            @Valid @RequestBody KakaoAuthRequest request
    ) {
        return ResponseEntity.ok(kakaoAuthService.authenticate(request));
    }

    @PostMapping("/oauth/kakao/signup")
    public ResponseEntity<KakaoAuthResponse> signupWithKakao(
            @Valid @RequestBody KakaoSignupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(kakaoAuthService.signup(request));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody TokenRefreshRequest request
    ) {
        return ResponseEntity.ok(tokenSessionService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody LogoutRequest request
    ) {
        tokenSessionService.logout(authorizationHeader, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset-links")
    public ResponseEntity<Void> requestPasswordResetLink(
            @Valid @RequestBody PasswordResetLinkRequest request
    ) {
        passwordResetService.requestResetLink(request);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
