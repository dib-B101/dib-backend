package com.b101.dib.address.command.controller;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.command.dto.UpdateAddressRequest;
import com.b101.dib.address.command.service.AddressCommandService;
import com.b101.dib.auth.token.AccessTokenClaims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/addresses")
@RequiredArgsConstructor
public class AddressCommandController {

    private final AddressCommandService addressCommandService;

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @Valid @RequestBody CreateAddressRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressCommandService.create(claims.memberId(), request));
    }

    @PatchMapping("/{addressId}")
    public ResponseEntity<AddressResponse> update(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @PathVariable Long addressId,
            @Valid @RequestBody UpdateAddressRequest request
    ) {
        return ResponseEntity.ok(
                addressCommandService.update(claims.memberId(), addressId, request)
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @PathVariable Long addressId
    ) {
        addressCommandService.delete(claims.memberId(), addressId);
        return ResponseEntity.noContent().build();
    }
}
