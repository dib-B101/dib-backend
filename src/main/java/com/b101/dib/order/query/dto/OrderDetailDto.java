package com.b101.dib.order.query.dto;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.product.domain.ProductCondition;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailDto {
    private Long orderId;
    private Long sellerId;
    private String sellerNickname;
    private Long buyerId;
    private String buyerNickname;
    private Long finalPrice;
    private OrderStatus status;
    private LocalDateTime paymentDue;
    private String address;
    private String trackingNumber;
    private String chattingSessionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long auctionId;
    private Long startPrice;
    private Long currentPrice;
    private AuctionStatus auctionStatus;
    private LocalDateTime auctionEndedAt;

    private Long productId;
    private String productTitle;
    private String thumbnailUrl;
    private ProductCondition condition;
    private String modelName;

    private Long paymentId;
    private Long paymentAmount;
    private String paymentType;
    private LocalDateTime paidAt;

    private Long settlementId;
    private Long netAmount;
    private LocalDateTime payoutAt;
}
