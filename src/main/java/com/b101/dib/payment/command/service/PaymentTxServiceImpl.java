package com.b101.dib.payment.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.repository.PaymentRepository;
import com.b101.dib.payment.toss.TossPaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentTxServiceImpl implements PaymentTxService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public Payment recordApproved(Long orderId, TossPaymentResponse res) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        try {
            order.pay();
        } catch (BusinessException e) {
            log.error("[결제 정합성] Toss 승인됐으나 주문 상태 변경 실패 orderId={} paymentKey={} code={}",
                    orderId, res.paymentKey(), e.getErrorCode());
            throw e;
        }
        Payment payment = paymentRepository.save(Payment.approved(order, res));
        notificationRepository.save(Notification.system(order.getBuyerId(), "결제 완료",
                "주문 #" + orderId + " 결제가 완료되었습니다. 배송지를 입력해 주세요."));
        return payment;
    }

    @Override
    public Payment recordRefunded(Long paymentId, String transactionKey) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
        payment.markRefunded(transactionKey != null ? transactionKey : "REFUNDED");
        order.refund();
        notificationRepository.save(Notification.system(order.getBuyerId(), "환불 완료",
                "주문 #" + order.getOrderId() + " 결제가 취소·환불되었습니다."));
        return payment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void recordFailed(Long orderId, Long buyerId, String reason) {
        notificationRepository.save(Notification.paymentFailed(buyerId, orderId, reason));
    }
}
