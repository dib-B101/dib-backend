package com.b101.dib.productImage.query.controller;

import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.productImage.storage.ProductImageStorage;
import com.b101.dib.productImage.storage.StoredImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 저장소가 local 이든 s3 든 이 경로로 내려간다. 프론트·AI 검수가 이 URL 을 그대로 쓰므로
// 경로와 응답 모양이 바뀌면 안 된다.
@WebMvcTest(ProductImageFileController.class)
@Import(SecurityConfig.class)
class ProductImageFileControllerTest {

    private static final String FILE_NAME = "3f1a2b4c-0000-4000-8000-000000000001.jpg";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProductImageStorage productImageStorage;
    @MockitoBean private AccessTokenVerifier accessTokenVerifier;

    @Test
    void streamsStoredImageWithoutLogin() throws Exception {
        byte[] bytes = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x00};
        given(productImageStorage.load(FILE_NAME))
                .willReturn(new StoredImage(new ByteArrayResource(bytes), "image/jpeg", bytes.length));

        mockMvc.perform(get(ProductImageStorage.PUBLIC_PATH + FILE_NAME))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/jpeg"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void returnsNotFoundWhenStorageHasNoSuchFile() throws Exception {
        given(productImageStorage.load(FILE_NAME)).willReturn(null);

        mockMvc.perform(get(ProductImageStorage.PUBLIC_PATH + FILE_NAME))
                .andExpect(status().isNotFound());
    }
}
