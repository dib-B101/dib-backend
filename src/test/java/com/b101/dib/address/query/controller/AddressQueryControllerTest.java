package com.b101.dib.address.query.controller;

import java.util.List;

import com.b101.dib.address.query.dto.AddressQueryDto;
import com.b101.dib.address.query.service.AddressQueryService;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.member.domain.MemberRole;
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

@WebMvcTest(AddressQueryController.class)
@Import(SecurityConfig.class)
class AddressQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressQueryService addressQueryService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void returnsAuthenticatedMemberAddresses() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(addressQueryService.findMine(1L)).willReturn(List.of(address()));

        mockMvc.perform(get("/api/v1/members/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].addressId").value(10))
                .andExpect(jsonPath("$.items[0].number").value("06236"))
                .andExpect(jsonPath("$.items[0].address").value("서울특별시 강남구 테헤란로"))
                .andExpect(jsonPath("$.items[0].name").value("회사"))
                .andExpect(jsonPath("$.items[0].apiAddressId").value("address-api-10"));

        verify(addressQueryService).findMine(1L);
    }

    @Test
    void returnsEmptyItemsWhenMemberHasNoAddress() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(addressQueryService.findMine(1L)).willReturn(List.of());

        mockMvc.perform(get("/api/v1/members/me/addresses")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void rejectsRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/addresses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private AddressQueryDto address() {
        AddressQueryDto address = new AddressQueryDto();
        address.setAddressId(10L);
        address.setNumber("06236");
        address.setAddress("서울특별시 강남구 테헤란로");
        address.setName("회사");
        address.setApiAddressId("address-api-10");
        return address;
    }
}
