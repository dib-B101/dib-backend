package com.b101.dib.member.command.controller;

import java.time.LocalDateTime;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.member.command.dto.WithdrawalRequest;
import com.b101.dib.member.command.dto.WithdrawalResponse;
import com.b101.dib.member.command.service.MemberWithdrawalService;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberWithdrawalController.class)
@Import(SecurityConfig.class)
class MemberWithdrawalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberWithdrawalService memberWithdrawalService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void acceptsWithdrawalRequestFromAuthenticatedMember() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        WithdrawalRequest request = new WithdrawalRequest("서비스 이용 종료");
        WithdrawalResponse response = new WithdrawalResponse(
                LocalDateTime.of(2026, 9, 16, 3, 0),
                LocalDateTime.of(2026, 9, 23, 3, 0),
                MemberStatus.WITHDRAWN
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberWithdrawalService.request(1L, request)).willReturn(response);

        mockMvc.perform(post("/api/v1/members/me/withdrawal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"서비스 이용 종료"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.requestedAt").value("2026-09-16T03:00:00"))
                .andExpect(jsonPath("$.scheduledAt").value("2026-09-23T03:00:00"))
                .andExpect(jsonPath("$.status").value("WITHDRAWN"));

        verify(memberWithdrawalService).request(1L, request);
    }

    @Test
    void acceptsWithdrawalRequestWithoutReason() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        WithdrawalRequest request = new WithdrawalRequest(null);
        WithdrawalResponse response = new WithdrawalResponse(
                LocalDateTime.of(2026, 9, 16, 3, 0),
                LocalDateTime.of(2026, 9, 23, 3, 0),
                MemberStatus.WITHDRAWN
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberWithdrawalService.request(1L, request)).willReturn(response);

        mockMvc.perform(post("/api/v1/members/me/withdrawal")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isAccepted());

        verify(memberWithdrawalService).request(1L, request);
    }

    @Test
    void rejectsWithdrawalRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(post("/api/v1/members/me/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
