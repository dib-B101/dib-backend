package com.b101.dib.order.query.service;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.PurchaseHistoryQueryDto;
import com.b101.dib.order.query.dto.PurchaseHistoryRowDto;
import com.b101.dib.order.repository.OrderMapper;
import com.b101.dib.payment.domain.PaymentType;
import com.b101.dib.product.domain.ProductCondition;
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
class MemberPurchaseQueryServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private MemberPurchaseQueryServiceImpl memberPurchaseQueryService;

    @Test
    void returnsPurchaseHistoryWithOptionalPaymentAndCursorPagination() {
        given(orderMapper.findPurchasesByBuyerId(1L, OrderStatus.PAID, 31L, 3))
                .willReturn(List.of(
                        purchaseRow(30L, 100L),
                        purchaseRow(29L, null),
                        purchaseRow(28L, null)
                ));

        CursorPageDto<PurchaseHistoryQueryDto> result = memberPurchaseQueryService.findMine(
                1L,
                OrderStatus.PAID,
                "31",
                2
        );

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getOrder().getOrderId()).isEqualTo(30L);
        assertThat(result.getItems().get(0).getPayment().getPaymentId()).isEqualTo(100L);
        assertThat(result.getItems().get(1).getOrder().getOrderId()).isEqualTo(29L);
        assertThat(result.getItems().get(1).getPayment()).isNull();
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo("29");
        verify(orderMapper).findPurchasesByBuyerId(1L, OrderStatus.PAID, 31L, 3);
    }

    @Test
    void rejectsInvalidCursor() {
        assertThatThrownBy(() -> memberPurchaseQueryService.findMine(1L, null, "invalid", 20))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode())
                                .isEqualTo(ErrorCode.INVALID_CURSOR)
                );
    }

    private PurchaseHistoryRowDto purchaseRow(Long orderId, Long paymentId) {
        PurchaseHistoryRowDto row = new PurchaseHistoryRowDto();
        row.setOrderId(orderId);
        row.setSellerId(2L);
        row.setSellerNickname("판매자");
        row.setFinalPrice(150_000L);
        row.setOrderStatus(OrderStatus.PAID);
        row.setAuctionId(20L);
        row.setStartPrice(100_000L);
        row.setCurrentPrice(150_000L);
        row.setAuctionStatus(AuctionStatus.ENDED);
        row.setProductId(10L);
        row.setProductTitle("구매 상품");
        row.setThumbnailUrl("https://example.com/product.jpg");
        row.setCondition(ProductCondition.GOOD);
        row.setModelName("MODEL-1");
        row.setPaymentId(paymentId);
        if (paymentId != null) {
            row.setPaymentAmount(150_000L);
            row.setPaymentType(PaymentType.CARD);
            row.setReceiptUrl("https://example.com/receipt");
        }
        return row;
    }
}
