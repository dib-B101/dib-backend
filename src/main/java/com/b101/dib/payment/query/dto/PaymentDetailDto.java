package com.b101.dib.payment.query.dto;

import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.payment.domain.PaymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDetailDto {
    private Long paymentId;
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private Long amount;
    private PaymentType type;
    private String receiptUrl;
    private LocalDateTime paidAt;
    private OrderStatus orderStatus;
}
