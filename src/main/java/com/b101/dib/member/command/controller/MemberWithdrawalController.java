package com.b101.dib.member.command.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.member.command.dto.WithdrawalRequest;
import com.b101.dib.member.command.dto.WithdrawalResponse;
import com.b101.dib.member.command.service.MemberWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/withdrawal")
@RequiredArgsConstructor
public class MemberWithdrawalController {

    private final MemberWithdrawalService memberWithdrawalService;

    @PostMapping
    public ResponseEntity<WithdrawalResponse> request(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @Valid @RequestBody(required = false) WithdrawalRequest request
    ) {
        WithdrawalRequest actualRequest = request == null ? new WithdrawalRequest(null) : request;
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(memberWithdrawalService.request(claims.memberId(), actualRequest));
    }
}
