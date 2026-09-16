package com.b101.dib.order.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseHistoryOrderDto {
    /** 주문 식별자 */
    private Long orderId;

    /** 판매 회원 식별자 */
    private Long sellerId;

    /** 판매 회원 닉네임 */
    private String sellerNickname;

    /** 최종 거래 금액 */
    private Long finalPrice;

    /** 주문 상태 */
    private OrderStatus status;

    /** 결제 기한 */
    private LocalDateTime paymentDue;

    /** 택배사 */
    private String carrier;

    /** 운송장 번호 */
    private String trackingNumber;

    /** 주문 생성 시각 */
    private LocalDateTime createdAt;
}
