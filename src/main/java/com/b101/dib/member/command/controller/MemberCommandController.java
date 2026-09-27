package com.b101.dib.member.command.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.command.service.MemberCommandService;
import com.b101.dib.member.command.service.MemberProfileImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/profile")
@RequiredArgsConstructor
public class MemberCommandController {

    private final MemberCommandService memberCommandService;
    private final MemberProfileImageService memberProfileImageService;

    @PatchMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UpdateProfileResponse> updateProfileImage(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(required = false) String nickname,
            @RequestPart("image") MultipartFile image
    ) {
        return ResponseEntity.ok(memberProfileImageService.updateImage(claims.memberId(), nickname, image));
    }

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
