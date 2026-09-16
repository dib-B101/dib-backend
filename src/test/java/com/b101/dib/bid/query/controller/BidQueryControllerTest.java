package com.b101.dib.bid.query.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.auth.token.AccessTokenVerifier;
import com.b101.dib.bid.query.dto.MyBidQueryDto;
import com.b101.dib.bid.query.service.BidQueryService;
import com.b101.dib.common.config.SecurityConfig;
import com.b101.dib.common.dto.CursorPageDto;
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

@WebMvcTest(BidQueryController.class)
@Import(SecurityConfig.class)
class BidQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BidQueryService bidQueryService;

    @MockitoBean
    private AccessTokenVerifier accessTokenVerifier;

    @Test
    void returnsAuthenticatedMemberBidHistory() throws Exception {
        AccessTokenClaims claims = new AccessTokenClaims(1L, MemberRole.USER);
        CursorPageDto<MyBidQueryDto> page = CursorPageDto.of(
                List.of(bid(30L), bid(29L)),
                1,
                MyBidQueryDto::getBidId
        );
        given(accessTokenVerifier.verifyBearer("Bearer access-token")).willReturn(claims);
        given(bidQueryService.findMine(1L, AuctionStatus.ACTIVE, "31", 1))
                .willReturn(page);

        mockMvc.perform(get("/api/v1/members/me/bids")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer access-token")
                        .param("status", "ACTIVE")
                        .param("cursor", "31")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].bidId").value(30))
                .andExpect(jsonPath("$.items[0].auctionId").value(10))
                .andExpect(jsonPath("$.items[0].amount").value(100000))
                .andExpect(jsonPath("$.items[0].createdAt").value("2026-09-16T03:00:00"))
                .andExpect(jsonPath("$.nextCursor").value("30"))
                .andExpect(jsonPath("$.hasNext").value(true));

        verify(bidQueryService).findMine(1L, AuctionStatus.ACTIVE, "31", 1);
    }

    @Test
    void rejectsBidHistoryRequestWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me/bids"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private MyBidQueryDto bid(Long bidId) {
        MyBidQueryDto dto = new MyBidQueryDto();
        dto.setBidId(bidId);
        dto.setAuctionId(10L);
        dto.setAmount(100_000L);
        dto.setCreatedAt(LocalDateTime.of(2026, 9, 16, 3, 0));
        return dto;
    }
}
