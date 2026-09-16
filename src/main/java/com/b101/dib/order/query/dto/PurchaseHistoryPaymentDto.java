package com.b101.dib.order.query.dto;

import com.b101.dib.payment.domain.PaymentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PurchaseHistoryPaymentDto {
    /** 결제 식별자 */
    private Long paymentId;

    /** 결제 금액 */
    private Long amount;

    /** 결제 수단 */
    private PaymentType type;

    /** 결제 영수증 URL */
    private String receiptUrl;

    /** 결제 완료 시각 */
    private LocalDateTime paidAt;
}
