package com.b101.dib.member.command.controller;

import java.time.LocalDateTime;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.command.service.MemberCommandService;
import com.b101.dib.member.domain.MemberRole;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberCommandController.class)
@Import(SecurityConfig.class)
class MemberCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberCommandService memberCommandService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void updatesAuthenticatedMemberProfile() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        UpdateProfileResponse response = new UpdateProfileResponse(
                1L,
                "새닉네임",
                "https://cdn.example.com/members/1/profile.webp",
                LocalDateTime.of(2026, 9, 15, 3, 0)
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest(
                        "새닉네임",
                        "https://cdn.example.com/members/1/profile.webp"
                )
        )).willReturn(response);

        mockMvc.perform(patch("/api/v1/members/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname":"새닉네임",
                                  "profileImageUrl":"https://cdn.example.com/members/1/profile.webp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(1))
                .andExpect(jsonPath("$.nickname").value("새닉네임"))
                .andExpect(jsonPath("$.profileImageUrl")
                        .value("https://cdn.example.com/members/1/profile.webp"))
                .andExpect(jsonPath("$.updatedAt").value("2026-09-15T03:00:00"));

        verify(memberCommandService).updateProfile(
                1L,
                new UpdateProfileRequest(
                        "새닉네임",
                        "https://cdn.example.com/members/1/profile.webp"
                )
        );
    }

    @Test
    void rejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(patch("/api/v1/members/me/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"새닉네임"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsBlankNickname() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);

        mockMvc.perform(patch("/api/v1/members/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    void updatesOnlyProfileImage() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        UpdateProfileResponse response = new UpdateProfileResponse(
                1L,
                "기존닉네임",
                "https://cdn.example.com/members/1/new-profile.webp",
                LocalDateTime.of(2026, 9, 15, 3, 0)
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberCommandService.updateProfile(
                1L,
                new UpdateProfileRequest(
                        null,
                        "https://cdn.example.com/members/1/new-profile.webp"
                )
        )).willReturn(response);

        mockMvc.perform(patch("/api/v1/members/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "profileImageUrl":"https://cdn.example.com/members/1/new-profile.webp"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("기존닉네임"))
                .andExpect(jsonPath("$.profileImageUrl")
                        .value("https://cdn.example.com/members/1/new-profile.webp"));

        verify(memberCommandService).updateProfile(
                1L,
                new UpdateProfileRequest(
                        null,
                        "https://cdn.example.com/members/1/new-profile.webp"
                )
        );
    }

    @Test
    void rejectsRequestWithoutAnyProfileField() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);

        mockMvc.perform(patch("/api/v1/members/me/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}
