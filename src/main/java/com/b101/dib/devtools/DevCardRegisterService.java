package com.b101.dib.devtools;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.payment.toss.TossBillingKeyResponse;
import com.b101.dib.paymentMethod.domain.PaymentMethod;
import com.b101.dib.paymentMethod.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Profile("local")
@RequiredArgsConstructor
@Transactional
public class DevCardRegisterService {
    private final PaymentMethodRepository paymentMethodRepository;
    private final MemberRepository memberRepository;
    private final DevTossCardClient devTossCardClient;

    public PaymentMethod register(Long memberId, DevCardRegisterRequest req) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (paymentMethodRepository.existsByMemberId(memberId)) {
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_ALREADY_EXISTS);
        }
        String customerKey = "dib-" + UUID.randomUUID();
        TossBillingKeyResponse res = devTossCardClient.issueByCard(customerKey, req.cardNumber(),
                req.cardExpirationYear(), req.cardExpirationMonth(), req.customerIdentityNumber());
        return paymentMethodRepository.save(PaymentMethod.of(memberId, customerKey, res));
    }
}
