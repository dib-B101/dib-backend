package com.b101.dib.auction.query.service;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.SaleHistoryQueryDto;
import com.b101.dib.auction.query.dto.SaleHistoryRowDto;
import com.b101.dib.auction.repository.AuctionMapper;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.product.domain.ProductStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberSaleQueryServiceImplTest {

    @Mock
    private AuctionMapper auctionMapper;

    @InjectMocks
    private MemberSaleQueryServiceImpl memberSaleQueryService;

    @Test
    void returnsSaleHistoryWithOptionalOrderAndCursorPagination() {
        given(auctionMapper.findSalesByMemberId(1L, AuctionStatus.ENDED, 31L, 3))
                .willReturn(List.of(
                        saleRow(30L, 100L),
                        saleRow(29L, null),
                        saleRow(28L, null)
                ));

        CursorPageDto<SaleHistoryQueryDto> result = memberSaleQueryService.findMine(
                1L,
                AuctionStatus.ENDED,
                "31",
                2
        );

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getAuction().getAuctionId()).isEqualTo(30L);
        assertThat(result.getItems().get(0).getOrder().getOrderId()).isEqualTo(100L);
        assertThat(result.getItems().get(1).getAuction().getAuctionId()).isEqualTo(29L);
        assertThat(result.getItems().get(1).getOrder()).isNull();
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("29");
        verify(auctionMapper).findSalesByMemberId(1L, AuctionStatus.ENDED, 31L, 3);
    }

    @Test
    void rejectsInvalidCursor() {
        assertThatThrownBy(() -> memberSaleQueryService.findMine(1L, null, "invalid", 20))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CURSOR)
                );
    }

    private SaleHistoryRowDto saleRow(Long auctionId, Long orderId) {
        SaleHistoryRowDto row = new SaleHistoryRowDto();
        row.setAuctionId(auctionId);
        row.setStartPrice(100_000L);
        row.setCurrentPrice(150_000L);
        row.setAuctionStatus(AuctionStatus.ENDED);
        row.setBidCount(5);
        row.setBidderCount(3);
        row.setProductId(10L);
        row.setProductTitle("판매 상품");
        row.setThumbnailUrl("https://example.com/product.jpg");
        row.setProductStatus(ProductStatus.SOLD);
        row.setOrderId(orderId);
        if (orderId != null) {
            row.setBuyerId(2L);
            row.setBuyerNickname("구매자");
            row.setFinalPrice(150_000L);
            row.setOrderStatus(OrderStatus.PAID);
        }
        return row;
    }
}
