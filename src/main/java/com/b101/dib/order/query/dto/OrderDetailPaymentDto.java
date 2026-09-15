package com.b101.dib.order.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailPaymentDto {
    private Long paymentId;
    private Long orderId;
    private Long amount;
    private String type;
    private LocalDateTime paidAt;
}
