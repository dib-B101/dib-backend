package com.b101.dib.paymentMethod.query.dto;

import com.b101.dib.payment.domain.PaymentType;
import com.b101.dib.paymentMethod.domain.PaymentMethod;

import java.time.LocalDateTime;

public record PaymentMethodResponse(Long paymentMethodId, PaymentType type, String cardCompany, String cardNumber,
                                    LocalDateTime createdAt) {
    public static PaymentMethodResponse from(PaymentMethod pm) {
        return new PaymentMethodResponse(pm.getPaymentMethodId(), pm.getType(), pm.getCardCompany(),
                pm.getCardNumber(), pm.getCreatedAt());
    }
}
