package com.b101.dib.member.query.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.MemberDetailDto;
import com.b101.dib.member.query.service.MemberQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberQueryController.class)
@Import(SecurityConfig.class)
class MemberQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberQueryService memberQueryService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void returnsAuthenticatedMemberInformation() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberQueryService.findMine(1L)).willReturn(member());

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.email").value("member@example.com"))
                .andExpect(jsonPath("$.name").value("김회원"))
                .andExpect(jsonPath("$.phoneNumber").value("01012345678"))
                .andExpect(jsonPath("$.nickname").value("회원"))
                .andExpect(jsonPath("$.profileImageUrl")
                        .value("https://cdn.example.com/members/1/profile.webp"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.birthDate").value("2000-01-02"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.score").value(36.5))
                .andExpect(jsonPath("$.lastLoginAt").value("2026-09-15T10:30:00"))
                .andExpect(jsonPath("$.bankName").value("한국은행"))
                .andExpect(jsonPath("$.accountHolder").value("김회원"))
                .andExpect(jsonPath("$.maskedAccountNumber").value("******7890"));

        verify(memberQueryService).findMine(1L);
    }

    @Test
    void rejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private MemberDetailDto member() {
        MemberDetailDto member = new MemberDetailDto();
        member.setMemberId(1L);
        member.setEmail("member@example.com");
        member.setName("김회원");
        member.setPhoneNumber("01012345678");
        member.setNickname("회원");
        member.setProfileImageUrl("https://cdn.example.com/members/1/profile.webp");
        member.setGender(Gender.MALE);
        member.setBirthDate(LocalDate.of(2000, 1, 2));
        member.setStatus(MemberStatus.ACTIVE);
        member.setRole(MemberRole.USER);
        member.setScore(36.5);
        member.setLastLoginAt(LocalDateTime.of(2026, 9, 15, 10, 30));
        member.setBankName("한국은행");
        member.setAccountHolder("김회원");
        member.setMaskedAccountNumber("******7890");
        return member;
    }
}
