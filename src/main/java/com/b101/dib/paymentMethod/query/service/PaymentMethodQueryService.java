package com.b101.dib.paymentMethod.query.service;

import com.b101.dib.paymentMethod.query.dto.PaymentMethodDetailDto;

public interface PaymentMethodQueryService {
    PaymentMethodDetailDto findMine(Long memberId);
}
