package com.b101.dib.paymentMethod.command.service;

import com.b101.dib.paymentMethod.domain.PaymentMethod;

public interface PaymentMethodCommandService {
    PaymentMethod register(Long memberId, String authKey, String customerKey);
    void delete(Long memberId);
}
