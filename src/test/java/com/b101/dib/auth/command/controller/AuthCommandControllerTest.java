package com.b101.dib.auth.command.controller;

import java.time.OffsetDateTime;

import com.b101.dib.auth.command.dto.LoginMemberResponse;
import com.b101.dib.auth.command.dto.LoginResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationResponse;
import com.b101.dib.auth.command.dto.PhoneVerificationConfirmResponse;
import com.b101.dib.auth.command.dto.SignupResponse;
import com.b101.dib.auth.command.service.LoginService;
import com.b101.dib.auth.command.service.PhoneVerificationService;
import com.b101.dib.auth.command.service.SignupService;
import com.b101.dib.auth.exception.InvalidVerificationCodeException;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.exception.RateLimitExceededException;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
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

    @MockitoBean
    private SignupService signupService;

    @MockitoBean
    private LoginService loginService;

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

    @Test
    void createsGeneralMemberAndReturnsTokens() throws Exception {
        given(signupService.signup(any()))
                .willReturn(new SignupResponse(
                        1L,
                        "user@example.com",
                        "길동이",
                        MemberStatus.ACTIVE,
                        MemberRole.USER,
                        "access-token",
                        "refresh-token"
                ));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"Password1!",
                                  "name":"홍길동",
                                  "nickname":"길동이",
                                  "gender":"MALE",
                                  "birthDate":"2000-01-01",
                                  "phoneNumber":"01012345678",
                                  "phoneVerificationToken":"verification-token",
                                  "deviceId":"device-id"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void rejectsSignupWithoutNickname() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"Password1!",
                                  "name":"홍길동",
                                  "gender":"MALE",
                                  "birthDate":"2000-01-01",
                                  "phoneNumber":"01012345678",
                                  "phoneVerificationToken":"verification-token",
                                  "deviceId":"device-id"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void rejectsSignupWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"Password1가",
                                  "name":"홍길동",
                                  "nickname":"길동이",
                                  "gender":"MALE",
                                  "birthDate":"2000-01-01",
                                  "phoneNumber":"01012345678",
                                  "phoneVerificationToken":"verification-token",
                                  "deviceId":"device-id"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PASSWORD"));
    }

    @Test
    void logsInGeneralMemberAndReturnsTokens() throws Exception {
        given(loginService.login(any()))
                .willReturn(new LoginResponse(
                        new LoginMemberResponse(
                                1L,
                                "user@example.com",
                                "길동이",
                                MemberStatus.ACTIVE,
                                MemberRole.USER
                        ),
                        "access-token",
                        "refresh-token",
                        1800
                ));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"Password1!",
                                  "deviceId":"device-id"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.memberId").value(1))
                .andExpect(jsonPath("$.member.email").value("user@example.com"))
                .andExpect(jsonPath("$.member.nickname").value("길동이"))
                .andExpect(jsonPath("$.member.status").value("ACTIVE"))
                .andExpect(jsonPath("$.member.role").value("USER"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.accessExpiresIn").value(1800));
    }

    @Test
    void rejectsLoginWithoutDeviceId() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"Password1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void returnsUnauthorizedForInvalidLoginCredentials() throws Exception {
        given(loginService.login(any()))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "password":"wrong-password",
                                  "deviceId":"device-id"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void returnsInvalidInputWhenLoginPasswordIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email":"user@example.com",
                                  "deviceId":"device-id"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
