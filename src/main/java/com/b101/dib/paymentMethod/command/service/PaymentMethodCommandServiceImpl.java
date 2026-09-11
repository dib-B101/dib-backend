package com.b101.dib.paymentMethod.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.payment.toss.TossBillingKeyResponse;
import com.b101.dib.payment.toss.TossPaymentsClient;
import com.b101.dib.paymentMethod.domain.PaymentMethod;
import com.b101.dib.paymentMethod.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentMethodCommandServiceImpl implements PaymentMethodCommandService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final MemberRepository memberRepository;
    private final TossPaymentsClient tossPaymentsClient;

    @Override
    public PaymentMethod register(Long memberId, String authKey, String customerKey) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (paymentMethodRepository.existsByMemberId(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_ALREADY_EXISTS);
        }
        TossBillingKeyResponse res = tossPaymentsClient.issueBillingKey(authKey, customerKey);
        return paymentMethodRepository.save(PaymentMethod.of(memberId, customerKey, res));
    }

    @Override
    public void delete(Long memberId) {
        PaymentMethod pm = paymentMethodRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        paymentMethodRepository.delete(pm);
    }
}
