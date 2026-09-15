package com.b101.dib.paymentMethod.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.paymentMethod.query.dto.PaymentMethodDetailDto;
import com.b101.dib.paymentMethod.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentMethodQueryServiceImpl implements PaymentMethodQueryService {
    private final PaymentMethodRepository paymentMethodRepository;

    @Override
    public PaymentMethodDetailDto findMine(Long memberId) {
        return paymentMethodRepository.findByMemberId(memberId)
                .map(PaymentMethodDetailDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
    }
}
