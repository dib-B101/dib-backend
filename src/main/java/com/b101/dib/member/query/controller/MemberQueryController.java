package com.b101.dib.member.query.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.member.query.dto.MemberDetailDto;
import com.b101.dib.member.query.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me")
@RequiredArgsConstructor
public class MemberQueryController {

    private final MemberQueryService memberQueryService;

    @GetMapping
    public ResponseEntity<MemberDetailDto> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims
    ) {
        return ResponseEntity.ok(memberQueryService.findMine(claims.memberId()));
    }
}
