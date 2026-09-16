package com.b101.dib.order.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.payment.domain.PaymentType;
import com.b101.dib.product.domain.ProductCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseHistoryRowDto {
    /** 주문 식별자 */
    private Long orderId;

    /** 판매 회원 식별자 */
    private Long sellerId;

    /** 판매 회원 닉네임 */
    private String sellerNickname;

    /** 최종 거래 금액 */
    private Long finalPrice;

    /** 주문 상태 */
    private OrderStatus orderStatus;

    /** 결제 기한 */
    private LocalDateTime paymentDue;

    /** 택배사 */
    private String carrier;

    /** 운송장 번호 */
    private String trackingNumber;

    /** 주문 생성 시각 */
    private LocalDateTime orderCreatedAt;

    /** 경매 식별자 */
    private Long auctionId;

    /** 경매 시작가 */
    private Long startPrice;

    /** 최종 낙찰가 */
    private Long currentPrice;

    /** 경매 상태 */
    private AuctionStatus auctionStatus;

    /** 경매 종료 시각 */
    private LocalDateTime auctionEndedAt;

    /** 상품 식별자 */
    private Long productId;

    /** 상품명 */
    private String productTitle;

    /** 상품 대표 이미지 URL */
    private String thumbnailUrl;

    /** 상품 상태 등급 */
    private ProductCondition condition;

    /** 상품 모델명 */
    private String modelName;

    /** 가장 최근 결제 식별자 */
    private Long paymentId;

    /** 결제 금액 */
    private Long paymentAmount;

    /** 결제 수단 */
    private PaymentType paymentType;

    /** 결제 영수증 URL */
    private String receiptUrl;

    /** 결제 완료 시각 */
    private LocalDateTime paidAt;
}
