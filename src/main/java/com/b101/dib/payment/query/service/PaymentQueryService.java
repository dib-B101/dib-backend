package com.b101.dib.payment.query.service;

import com.b101.dib.payment.query.dto.PaymentDetailDto;

public interface PaymentQueryService {
    PaymentDetailDto findDetail(Long memberId, Long paymentId);
}
