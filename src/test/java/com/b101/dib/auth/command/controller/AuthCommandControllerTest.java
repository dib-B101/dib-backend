package com.b101.dib.auth.command.controller;

import java.time.OffsetDateTime;

import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.exception.InvalidVerificationCodeException;
import com.b101.dib.auth.command.service.PhoneVerificationService;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthCommandController.class)
@Import(SecurityConfig.class)
class AuthCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PhoneVerificationService phoneVerificationService;

    @Test
    void acceptsPhoneVerificationRequest() throws Exception {
        given(phoneVerificationService.request(any()))
                .willReturn(new PhoneVerificationResponse(
                        "verification-id",
                        OffsetDateTime.parse("2026-09-08T18:03:00+09:00"),
                        60
                ));

        mockMvc.perform(post("/api/v1/auth/phone-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phoneNumber":"010-1234-5678","purpose":"SIGN_UP"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.verificationId").value("verification-id"))
                .andExpect(jsonPath("$.retryAfterSeconds").value(60));
    }

    @Test
    void returnsInvalidPhoneForUnsupportedPhoneNumber() throws Exception {
        given(phoneVerificationService.request(any()))
                .willThrow(new BusinessException(ErrorCode.INVALID_PHONE));

        mockMvc.perform(post("/api/v1/auth/phone-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phoneNumber":"02-1234-5678","purpose":"SIGN_UP"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PHONE"));
    }

    @Test
    void returnsRetryAfterWhenRequestIsRateLimited() throws Exception {
        given(phoneVerificationService.request(any()))
                .willThrow(new RateLimitExceededException(45));

        mockMvc.perform(post("/api/v1/auth/phone-verifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"phoneNumber":"01012345678","purpose":"SIGN_UP"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "45"))
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
    }

    @Test
    void confirmsPhoneVerificationCode() throws Exception {
        given(phoneVerificationService.confirm(any(), any()))
                .willReturn(new PhoneVerificationConfirmResponse(
                        "verification-token",
                        OffsetDateTime.parse("2026-09-08T18:10:00+09:00")
                ));

        mockMvc.perform(post("/api/v1/auth/phone-verifications/verification-id/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationToken").value("verification-token"))
                .andExpect(jsonPath("$.expiresAt").value("2026-09-08T18:10:00+09:00"));
    }

    @Test
    void returnsRemainingAttemptsForInvalidCode() throws Exception {
        given(phoneVerificationService.confirm(any(), any()))
                .willThrow(new InvalidVerificationCodeException(3));

        mockMvc.perform(post("/api/v1/auth/phone-verifications/verification-id/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"000000"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CODE"))
                .andExpect(jsonPath("$.remainingAttempts").value(3));
    }

    @Test
    void returnsGoneForExpiredVerification() throws Exception {
        given(phoneVerificationService.confirm(any(), any()))
                .willThrow(new BusinessException(ErrorCode.VERIFICATION_EXPIRED));

        mockMvc.perform(post("/api/v1/auth/phone-verifications/verification-id/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"123456"}
                                """))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("VERIFICATION_EXPIRED"));
    }

    @Test
    void returnsBadRequestForInvalidVerificationId() throws Exception {
        given(phoneVerificationService.confirm(any(), any()))
                .willThrow(new BusinessException(ErrorCode.INVALID_VERIFICATION_ID));

        mockMvc.perform(post("/api/v1/auth/phone-verifications/tampered/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"123456"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_VERIFICATION_ID"));
    }

    @Test
    void returnsTooManyRequestsAfterAttemptLimit() throws Exception {
        given(phoneVerificationService.confirm(any(), any()))
                .willThrow(new BusinessException(ErrorCode.ATTEMPTS_EXCEEDED));

        mockMvc.perform(post("/api/v1/auth/phone-verifications/verification-id/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"000000"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("ATTEMPTS_EXCEEDED"));
    }
}
