package com.b101.dib.order.query.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.PurchaseHistoryAuctionDto;
import com.b101.dib.order.query.dto.PurchaseHistoryOrderDto;
import com.b101.dib.order.query.dto.PurchaseHistoryProductDto;
import com.b101.dib.order.query.dto.PurchaseHistoryQueryDto;
import com.b101.dib.order.query.service.MemberPurchaseQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MemberPurchaseQueryController.class)
@Import(SecurityConfig.class)
class MemberPurchaseQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberPurchaseQueryService memberPurchaseQueryService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void returnsAuthenticatedMemberPurchaseHistory() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        CursorPageDto<PurchaseHistoryQueryDto> page = CursorPageDto.of(
                List.of(purchase(30L), purchase(29L)),
                1,
                row -> row.getOrder().getOrderId()
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberPurchaseQueryService.findMine(1L, OrderStatus.PAID, "31", 1))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/members/me/purchases")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("orderStatus", "PAID")
                        .param("cursor", "31")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].order.orderId").value(30))
                .andExpect(jsonPath("$.data.items[0].order.status").value("PAID"))
                .andExpect(jsonPath("$.data.items[0].auction.auctionId").value(20))
                .andExpect(jsonPath("$.data.items[0].product.productId").value(10))
                .andExpect(jsonPath("$.data.items[0].product.title").value("구매 상품"))
                .andExpect(jsonPath("$.data.items[0].payment").doesNotExist())
                .andExpect(jsonPath("$.data.nextCursor").value("30"))
                .andExpect(jsonPath("$.data.hasNext").value(true));

        verify(memberPurchaseQueryService).findMine(1L, OrderStatus.PAID, "31", 1);
    }

    @Test
    void rejectsPurchaseHistoryRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/purchases"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private PurchaseHistoryQueryDto purchase(Long orderId) {
        PurchaseHistoryOrderDto order = new PurchaseHistoryOrderDto();
        order.setOrderId(orderId);
        order.setStatus(OrderStatus.PAID);

        PurchaseHistoryAuctionDto auction = new PurchaseHistoryAuctionDto();
        auction.setAuctionId(20L);

        PurchaseHistoryProductDto product = new PurchaseHistoryProductDto();
        product.setProductId(10L);
        product.setTitle("구매 상품");

        PurchaseHistoryQueryDto dto = new PurchaseHistoryQueryDto();
        dto.setOrder(order);
        dto.setAuction(auction);
        dto.setProduct(product);
        return dto;
    }
}
