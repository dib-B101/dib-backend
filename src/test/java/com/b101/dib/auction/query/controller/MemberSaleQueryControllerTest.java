package com.b101.dib.auction.query.controller;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.SaleHistoryAuctionDto;
import com.b101.dib.auction.query.dto.SaleHistoryProductDto;
import com.b101.dib.auction.query.dto.SaleHistoryQueryDto;
import com.b101.dib.auction.query.service.MemberSaleQueryService;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.product.domain.ProductStatus;
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

@WebMvcTest(MemberSaleQueryController.class)
@Import(SecurityConfig.class)
class MemberSaleQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberSaleQueryService memberSaleQueryService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void returnsAuthenticatedMemberSaleHistory() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        CursorPageDto<SaleHistoryQueryDto> page = CursorPageDto.of(
                List.of(sale(30L), sale(29L)),
                1,
                row -> row.getAuction().getAuctionId()
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(memberSaleQueryService.findMine(1L, AuctionStatus.ACTIVE, "31", 1))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/members/me/sales")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("auctionStatus", "ACTIVE")
                        .param("cursor", "31")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].auction.auctionId").value(30))
                .andExpect(jsonPath("$.items[0].auction.status").value("ACTIVE"))
                .andExpect(jsonPath("$.items[0].product.productId").value(10))
                .andExpect(jsonPath("$.items[0].product.title").value("판매 상품"))
                .andExpect(jsonPath("$.items[0].order").doesNotExist())
                .andExpect(jsonPath("$.nextCursor").value("30"))
                .andExpect(jsonPath("$.hasNext").value(true));

        verify(memberSaleQueryService).findMine(1L, AuctionStatus.ACTIVE, "31", 1);
    }

    @Test
    void rejectsSaleHistoryRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/sales"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private SaleHistoryQueryDto sale(Long auctionId) {
        SaleHistoryAuctionDto auction = new SaleHistoryAuctionDto();
        auction.setAuctionId(auctionId);
        auction.setStatus(AuctionStatus.ACTIVE);

        SaleHistoryProductDto product = new SaleHistoryProductDto();
        product.setProductId(10L);
        product.setTitle("판매 상품");
        product.setStatus(ProductStatus.ON_AUCTION);

        SaleHistoryQueryDto dto = new SaleHistoryQueryDto();
        dto.setAuction(auction);
        dto.setProduct(product);
        return dto;
    }
}
