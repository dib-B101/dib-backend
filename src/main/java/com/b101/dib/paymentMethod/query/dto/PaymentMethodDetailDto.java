package com.b101.dib.paymentMethod.query.dto;

import com.b101.dib.payment.domain.PaymentType;
import com.b101.dib.paymentMethod.domain.PaymentMethod;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentMethodDetailDto {
    private Long paymentMethodId;
    private PaymentType type;
    private String cardCompany;
    private String cardNumber;
    private LocalDateTime createdAt;

    public static PaymentMethodDetailDto from(PaymentMethod pm) {
        PaymentMethodDetailDto dto = new PaymentMethodDetailDto();
        dto.setPaymentMethodId(pm.getPaymentMethodId());
        dto.setType(pm.getType());
        dto.setCardCompany(pm.getCardCompany());
        dto.setCardNumber(pm.getCardNumber());
        dto.setCreatedAt(pm.getCreatedAt());
        return dto;
    }
}
