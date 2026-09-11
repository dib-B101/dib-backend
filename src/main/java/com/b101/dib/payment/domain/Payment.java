package com.b101.dib.payment.domain;

import com.b101.dib.order.domain.Order;
import com.b101.dib.payment.toss.TossPaymentResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long orderId;
    private Long buyerId;
    private Long amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentType type;

    private String paymentKey;
    private String refundKey;
    private String receiptUrl;
    private LocalDateTime paidAt;

    public static Payment approved(Order order, TossPaymentResponse res) {
        return Payment.builder()
                .orderId(order.getOrderId())
                .buyerId(order.getBuyerId())
                .amount(order.getFinalPrice())
                .type(PaymentType.CARD)
                .paymentKey(res.paymentKey())
                .receiptUrl(res.receipt() != null ? res.receipt().url() : null)
                .paidAt(LocalDateTime.now())
                .build();
    }
}
