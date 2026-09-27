package com.b101.dib.payment.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "payment")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private static final ZoneId PAYMENT_ZONE = ZoneId.of("Asia/Seoul");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long orderId;
    private Long buyerId;
    private Long amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentType type;

    @JsonIgnore
    private String paymentKey;
    @JsonIgnore
    private String refundKey;
    private String receiptUrl;
    private LocalDateTime paidAt;

    public static Payment approved(Order order, TossPaymentResponse res) {
        LocalDateTime paidAt = res.approvedAt() == null || res.approvedAt().isBlank()
                ? LocalDateTime.now(PAYMENT_ZONE)
                : OffsetDateTime.parse(res.approvedAt()).atZoneSameInstant(PAYMENT_ZONE).toLocalDateTime();
        return Payment.builder()
                .orderId(order.getOrderId())
                .buyerId(order.getBuyerId())
                .amount(order.getFinalPrice())
                .type(PaymentType.CARD)
                .paymentKey(res.paymentKey())
                .receiptUrl(res.receipt() != null ? res.receipt().url() : null)
                .paidAt(paidAt)
                .build();
    }

    public boolean isRefunded() {
        return refundKey != null;
    }

    public void markRefunded(String transactionKey) {
        this.refundKey = transactionKey;
    }
}
