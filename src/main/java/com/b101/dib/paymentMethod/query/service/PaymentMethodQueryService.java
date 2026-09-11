package com.b101.dib.paymentMethod.query.service;

import com.b101.dib.paymentMethod.query.dto.PaymentMethodResponse;

public interface PaymentMethodQueryService {
    PaymentMethodResponse findMine(Long memberId);
}
