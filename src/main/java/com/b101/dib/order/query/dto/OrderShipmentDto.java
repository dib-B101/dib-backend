package com.b101.dib.order.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderShipmentDto {
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private OrderStatus status;
    private String carrier;
    private String trackingNumber;
    private String address;
}
