package com.b101.dib.address.command.controller;

import com.b101.dib.address.command.dto.AddressResponse;
import com.b101.dib.address.command.dto.CreateAddressRequest;
import com.b101.dib.address.command.service.AddressCommandService;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AddressCommandController.class)
@Import({SecurityConfig.class, AddressExceptionHandler.class})
class AddressCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressCommandService addressCommandService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void createsAddressForAuthenticatedMember() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        CreateAddressRequest request = new CreateAddressRequest(
                "06236",
                "서울특별시 강남구 테헤란로",
                "회사",
                "address-api-10"
        );
        AddressResponse response = new AddressResponse(
                10L,
                "06236",
                "서울특별시 강남구 테헤란로",
                "회사",
                "address-api-10"
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(addressCommandService.create(1L, request)).willReturn(response);

        mockMvc.perform(post("/api/v1/members/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "number":"06236",
                                  "address":"서울특별시 강남구 테헤란로",
                                  "name":"회사",
                                  "apiAddressId":"address-api-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressId").value(10))
                .andExpect(jsonPath("$.number").value("06236"))
                .andExpect(jsonPath("$.address").value("서울특별시 강남구 테헤란로"))
                .andExpect(jsonPath("$.name").value("회사"))
                .andExpect(jsonPath("$.apiAddressId").value("address-api-10"));

        verify(addressCommandService).create(1L, request);
    }

    @Test
    void rejectsInvalidAddress() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);

        mockMvc.perform(post("/api/v1/members/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":" ",
                                  "apiAddressId":""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ADDRESS"));
    }

    @Test
    void rejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(post("/api/v1/members/me/addresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"회사",
                                  "apiAddressId":"address-api-10"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
