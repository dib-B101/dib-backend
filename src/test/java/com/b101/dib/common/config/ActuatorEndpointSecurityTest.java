package com.b101.dib.common.config;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.productImage.query.controller.ProductImageFileController;
import com.b101.dib.productImage.storage.ProductImageStorage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ALB 가 인터넷에 붙어 있어서 /actuator 아래가 그대로 공개되면 엔드포인트 목록·요청량·JVM 내부가 샌다.
// 반대로 헬스까지 막으면 ALB 대상 검사와 K8s probe 가 죽어 Pod 가 트래픽을 못 받는다. 둘 다 확인한다.
//
// 슬라이스 테스트라 actuator 핸들러 자체는 없다. 인가는 핸들러 조회보다 먼저 도므로
// "막혔나(401/403)" 와 "통과했나(핸들러 없어서 404)" 로 규칙을 구분할 수 있다.
@WebMvcTest(ProductImageFileController.class)
@Import(SecurityConfig.class)
class ActuatorEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AccessTokenVerifier accessTokenVerifier;
    @MockitoBean private ProductImageStorage productImageStorage;

    @Test
    void healthProbesStayOpenForAlbAndKubelet() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/health/readiness")).andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isNotFound());
    }

    @Test
    void rejectsMetricsWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMetricsForOrdinaryMember() throws Exception {
        given(accessTokenVerifier.verifyBearer("Bearer access-token"))
                .willReturn(new AccessTokenClaims(1L, MemberRole.USER));

        mockMvc.perform(get("/actuator/prometheus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsMetricsForAdmin() throws Exception {
        given(accessTokenVerifier.verifyBearer("Bearer access-token"))
                .willReturn(new AccessTokenClaims(1L, MemberRole.ADMIN));

        mockMvc.perform(get("/actuator/prometheus")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isNotFound());   // 인가 통과 — 슬라이스라 핸들러만 없다
    }
}
