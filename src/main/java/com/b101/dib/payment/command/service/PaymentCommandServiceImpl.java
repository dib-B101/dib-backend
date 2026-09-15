package com.b101.dib.payment.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.repository.PaymentRepository;
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
public class PaymentCommandServiceImpl implements PaymentCommandService {   // 외부 HTTP를 부르므로 @Transactional 없음
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
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
        try {
            return paymentTxService.recordApproved(orderId, res);
        } catch (BusinessException e) {
            compensate(res.paymentKey(), orderId);
            throw e;
        }
    }

    // 승인은 됐는데 우리 DB 반영이 실패한 경우 — 돈만 나간 상태를 막기 위해 즉시 Toss 취소
    private void compensate(String paymentKey, Long orderId) {
        try {
            tossPaymentsClient.cancel(paymentKey, "주문 상태 불일치로 자동 취소 (orderId=" + orderId + ")", null);
            log.warn("[결제 정합성] 승인 건 자동 취소 완료 paymentKey={} orderId={}", paymentKey, orderId);
        } catch (Exception ex) {
            log.error("[결제 정합성] 자동 취소 실패 — 수동 확인 필요 paymentKey={} orderId={}", paymentKey, orderId, ex);
        }
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

    @Override
    public Payment refund(Long paymentId, String reason, Long amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        if (payment.isRefunded() || payment.getPaymentKey() == null
                || (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.SHIPPED
                && order.getStatus() != OrderStatus.DELIEVERED)) {
            throw new BusinessException(ErrorCode.REFUND_NOT_ALLOWED);
        }
        if (amount != null && (amount <= 0 || amount > payment.getAmount())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        TossPaymentResponse res = tossPaymentsClient.cancel(payment.getPaymentKey(), reason, amount);
        return paymentTxService.recordRefunded(paymentId, res.lastTransactionKey());
    }
}
