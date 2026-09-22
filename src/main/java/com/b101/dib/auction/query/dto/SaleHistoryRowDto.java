package com.b101.dib.auction.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.product.domain.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class SaleHistoryRowDto {
    /** 경매 식별자 */
    private Long auctionId;

    /** 경매 시작가 */
    private Long startPrice;

    /** 현재가 또는 최종 낙찰가 */
    private Long currentPrice;

    /** 경매 상태 */
    private AuctionStatus auctionStatus;

    /** 경매 진행 시간(초) */
    private Integer auctionTime;

    /** 누적 입찰 횟수 */
    private Integer bidCount;

    /** 경매 참여자 수 */
    private Integer bidderCount;

    /** 경매 시작 시각 */
    private LocalDateTime startedAt;

    /** 경매 종료 시각 */
    private LocalDateTime endedAt;

    /** 상품 식별자 */
    private Long productId;

    /** 상품명 */
    private String productTitle;

    /** 상품 대표 이미지 URL */
    private String thumbnailUrl;

    /** 상품 상태 */
    private ProductStatus productStatus;

    /** 가장 최근 주문 식별자 */
    private Long orderId;

    /** 구매 회원 식별자 */
    private Long buyerId;

    /** 구매 회원 닉네임 */
    private String buyerNickname;

    /** 최종 거래 금액 */
    private Long finalPrice;

    /** 주문 상태 */
    private OrderStatus orderStatus;

    /** 주문 생성 시각 */
    private LocalDateTime orderCreatedAt;
}
