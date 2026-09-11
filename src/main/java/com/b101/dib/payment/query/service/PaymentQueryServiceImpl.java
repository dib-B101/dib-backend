package com.b101.dib.payment.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.payment.query.dto.PaymentDetailDto;
import com.b101.dib.payment.repository.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryServiceImpl implements PaymentQueryService {
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDetailDto findDetail(Long memberId, Long paymentId) {
        PaymentDetailDto dto = paymentMapper.findById(paymentId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        if (!dto.getBuyerId().equals(memberId) && !dto.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return dto;
    }
}
