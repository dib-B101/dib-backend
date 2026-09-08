package com.b101.dib.auth.query.controller;

import com.b101.dib.auth.query.dto.EmailAvailabilityRequest;
import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.service.AuthQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthQueryController {

    private final AuthQueryService authQueryService;

    @GetMapping("/emails/availability")
    public ResponseEntity<EmailAvailabilityResponse> checkEmailAvailability(
            @Valid @ModelAttribute EmailAvailabilityRequest request
    ) {
        return ResponseEntity.ok(authQueryService.checkEmailAvailability(request.email()));
    }
}
