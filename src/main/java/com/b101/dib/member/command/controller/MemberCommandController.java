package com.b101.dib.member.command.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.command.service.MemberCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/profile")
@RequiredArgsConstructor
public class MemberCommandController {

    private final MemberCommandService memberCommandService;

    @PatchMapping
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                memberCommandService.updateProfile(claims.memberId(), request)
        );
    }
}
