package com.b101.dib.order.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailOrderDto {
    private Long orderId;
    private Long auctionId;
    private Long productId;
    private Long sellerId;
    private String sellerNickname;
    // 구매 거래 상세의 판매자 줄에 아바타를 그린다
    private String sellerProfileImageUrl;
    private Long buyerId;
    private String buyerNickname;
    private Long finalPrice;
    private OrderStatus status;
    private LocalDateTime paymentDue;
    private String address;
    private String carrier;
    private String trackingNumber;
    private String chattingSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 신고 보류. status는 그대로이므로 프론트는 이 값으로 "신고 처리 중" 배지와 버튼 비활성을 판단한다
    private LocalDateTime heldAt;
    private Long holdReportId;
}
