package com.b101.dib.order.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderQueryDto {
    private Long orderId;
    private Long auctionId;
    private Long productId;
    private String productTitle;
    private String thumbnailUrl;
    private Long sellerId;
    private String sellerNickname;
    private Long buyerId;
    private String buyerNickname;
    private Long finalPrice;
    private OrderStatus status;
    private LocalDateTime paymentDue;
    private String trackingNumber;
    private LocalDateTime createdAt;
}
