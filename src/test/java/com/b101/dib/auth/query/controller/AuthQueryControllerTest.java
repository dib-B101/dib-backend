package com.b101.dib.auth.query.controller;

import com.b101.dib.auth.query.dto.EmailAvailabilityResponse;
import com.b101.dib.auth.query.service.AuthQueryService;
import com.b101.dib.common.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthQueryController.class)
@Import(SecurityConfig.class)
class AuthQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthQueryService authQueryService;

    @Test
    void returnsAvailabilityForValidEmail() throws Exception {
        given(authQueryService.checkEmailAvailability("member@example.com"))
                .willReturn(new EmailAvailabilityResponse(true));

        mockMvc.perform(get("/api/v1/auth/emails/availability")
                        .param("email", "member@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void returnsInvalidEmailWhenEmailFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/auth/emails/availability")
                        .param("email", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EMAIL"));

        verifyNoInteractions(authQueryService);
    }

    @Test
    void returnsInvalidEmailWhenEmailIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/emails/availability"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EMAIL"));

        verifyNoInteractions(authQueryService);
    }
}
