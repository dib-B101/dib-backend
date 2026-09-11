package com.b101.dib.paymentMethod.domain;

import com.b101.dib.payment.domain.PaymentType;
import com.b101.dib.payment.toss.TossBillingKeyResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_method")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentMethodId;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private PaymentType type;

    private String billingKey;
    private String customerKey;
    private String cardCompany;
    private String cardNumber;
    private LocalDateTime createdAt;

    public static PaymentMethod of(Long memberId, String customerKey, TossBillingKeyResponse res) {
        return PaymentMethod.builder()
                .memberId(memberId)
                .type(PaymentType.CARD)
                .customerKey(customerKey)
                .billingKey(res.billingKey())
                .cardCompany(res.cardCompany())
                .cardNumber(res.card() != null ? res.card().number() : null)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
