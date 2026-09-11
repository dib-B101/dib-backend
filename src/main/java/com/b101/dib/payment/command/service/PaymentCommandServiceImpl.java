package com.b101.dib.payment.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.toss.TossApiException;
import com.b101.dib.payment.toss.TossPaymentResponse;
import com.b101.dib.payment.toss.TossPaymentsClient;
import com.b101.dib.paymentMethod.domain.PaymentMethod;
import com.b101.dib.paymentMethod.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandServiceImpl implements PaymentCommandService {
    private final OrderRepository orderRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentTxService paymentTxService;

    @Override
    public Payment autoCharge(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isPayable()) {
            throw new BusinessException(order.getStatus() == OrderStatus.PAID
                    ? ErrorCode.DUPLICATE_PAYMENT : ErrorCode.PAYMENT_DEADLINE_EXPIRED);
        }
        PaymentMethod pm = paymentMethodRepository.findByMemberId(order.getBuyerId()).orElse(null);
        if (pm == null) {
            paymentTxService.recordFailed(orderId, order.getBuyerId(), "등록된 결제 수단이 없습니다");
            throw new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }
        String tossOrderId = "dib-" + orderId + "-" + System.currentTimeMillis();
        TossPaymentResponse res;
        try {
            res = tossPaymentsClient.chargeBilling(pm.getBillingKey(), pm.getCustomerKey(),
                    order.getFinalPrice(), tossOrderId, "DIB 낙찰 상품 #" + orderId);
        } catch (TossApiException e) {
            paymentTxService.recordFailed(orderId, order.getBuyerId(), e.getTossMessage());
            throw e;
        }
        if (!"DONE".equals(res.status())) {
            paymentTxService.recordFailed(orderId, order.getBuyerId(), "승인 상태가 DONE이 아닙니다: " + res.status());
            throw new TossApiException(ErrorCode.TOSS_CONFIRM_FAILED, res.status());
        }
        return paymentTxService.recordApproved(orderId, res);
    }

    @Override
    public Payment retry(Long memberId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.isBuyer(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return autoCharge(orderId);
    }
}
