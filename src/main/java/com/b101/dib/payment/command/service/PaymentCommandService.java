package com.b101.dib.payment.command.service;

import com.b101.dib.payment.domain.Payment;

public interface PaymentCommandService {
    Payment autoCharge(Long orderId);
    Payment retry(Long memberId, Long orderId);
}
