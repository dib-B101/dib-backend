package com.b101.dib.address.query.controller;

import com.b101.dib.address.query.dto.AddressListResponse;
import com.b101.dib.address.query.service.AddressQueryService;
import com.b101.dib.auth.token.AccessTokenClaims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/addresses")
@RequiredArgsConstructor
public class AddressQueryController {

    private final AddressQueryService addressQueryService;

    @GetMapping
    public ResponseEntity<AddressListResponse> findMine(
            @AuthenticationPrincipal AccessTokenClaims claims
    ) {
        return ResponseEntity.ok(
                new AddressListResponse(addressQueryService.findMine(claims.memberId()))
        );
    }
}
