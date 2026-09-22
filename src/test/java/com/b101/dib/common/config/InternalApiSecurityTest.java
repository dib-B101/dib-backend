package com.b101.dib.common.config;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.order.command.controller.OrderInternalController;
import com.b101.dib.order.command.service.OrderCommandService;
import com.b101.dib.order.query.service.OrderInternalQueryService;
import com.b101.dib.payment.command.service.PaymentCommandService;
import com.b101.dib.settlement.command.controller.InternalSettlementController;
import com.b101.dib.settlement.command.service.SettlementCommandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// /api/v1/internal/** 은 돈이 움직이는 경로다 — 주문 생성은 낙찰자 카드로 결제까지 가고,
// 정산 실행은 판매자에게 지급한다. 회원 식별 헤더도 안 받으므로 인가가 유일한 방어선이다.
// SecurityConfig 가 기본 개방(anyRequest().permitAll())이라 목록에서 빠지면 조용히 다시 열린다.
@WebMvcTest({OrderInternalController.class, InternalSettlementController.class})
@Import(SecurityConfig.class)
class InternalApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private AccessTokenVerifier accessTokenVerifier;
    @MockitoBean private SettlementCommandService settlementCommandService;
    @MockitoBean private OrderCommandService orderCommandService;
    @MockitoBean private OrderInternalQueryService orderInternalQueryService;
    @MockitoBean private PaymentCommandService paymentCommandService;

    @Test
    void rejectsSettlementPayoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/internal/settlements/1/execute"))
                .andExpect(status().isUnauthorized());

        verify(settlementCommandService, never()).execute(1L);
    }

    @Test
    void rejectsOrderCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/api/v1/internal/auctions/1/orders"))
                .andExpect(status().isUnauthorized());

        verify(orderCommandService, never()).create(1L);
    }

    @Test
    void rejectsSettlementPayoutForOrdinaryMember() throws Exception {
        givenRole(MemberRole.USER);

        mockMvc.perform(post("/api/v1/internal/settlements/1/execute")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isForbidden());

        verify(settlementCommandService, never()).execute(1L);
    }

    @Test
    void rejectsOrderCreationForOrdinaryMember() throws Exception {
        givenRole(MemberRole.USER);

        mockMvc.perform(post("/api/v1/internal/auctions/1/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isForbidden());

        verify(orderCommandService, never()).create(1L);
    }

    @Test
    void allowsSettlementPayoutForAdmin() throws Exception {
        givenRole(MemberRole.ADMIN);

        mockMvc.perform(post("/api/v1/internal/settlements/1/execute")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(status().isAccepted());

        verify(settlementCommandService).execute(1L);
    }

    private void givenRole(MemberRole role) {
        given(accessTokenVerifier.verifyBearer("Bearer access-token"))
                .willReturn(new AccessTokenClaims(1L, role));
    }
}
