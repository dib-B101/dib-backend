package com.b101.dib.product.command.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.command.service.ProductCommandService;
import com.b101.dib.product.domain.Product;
import com.b101.dib.product.domain.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductCommandController.class)
@Import(SecurityConfig.class)
class ProductCommandControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ProductCommandService productCommandService;
    @MockitoBean AccessTokenVerifier accessTokenVerifier;

    @Test
    void createsProductForAuthenticatedMember() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(17L, MemberRole.USER);
        Product product = Product.builder()
                .productId(8L)
                .memberId(17L)
                .status(ProductStatus.PENDING)
                .createdAt(LocalDateTime.of(2026, 9, 17, 10, 0))
                .build();
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(productCommandService.create(eq(17L), any(ProductCreateRequest.class), any()))
                .willReturn(product);

        mockMvc.perform(multipart("/api/v1/products")
                        .file(requestPart())
                        .file(imagePart())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.productId").value(8))
                .andExpect(jsonPath("$.data.memberId").value(17));

        verify(productCommandService).create(eq(17L), any(ProductCreateRequest.class), any());
    }

    @Test
    void rejectsProductCreationWithoutAccessToken() throws Exception {
        mockMvc.perform(multipart("/api/v1/products")
                        .file(requestPart())
                        .file(imagePart()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private MockMultipartFile requestPart() {
        return new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "categoryId":3,
                  "title":"필름 카메라",
                  "description":"정상 작동합니다.",
                  "condition":"GOOD",
                  "releaseYear":1982,
                  "marketPrice":120000,
                  "startPrice":30000,
                  "auctionTime":300
                }
                """.getBytes()
        );
    }

    private MockMultipartFile imagePart() {
        return new MockMultipartFile(
                "images", "camera.jpg", "image/jpeg",
                new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00}
        );
    }
}
