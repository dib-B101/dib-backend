package com.b101.dib.auction.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SaleHistoryOrderDto {
    /** 주문 식별자 */
    private Long orderId;

    /** 구매 회원 식별자 */
    private Long buyerId;

    /** 구매 회원 닉네임 */
    private String buyerNickname;

    /** 최종 거래 금액 */
    private Long finalPrice;

    /** 주문 상태 */
    private OrderStatus status;

    /** 주문 생성 시각 */
    private LocalDateTime createdAt;
}
