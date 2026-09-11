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
public class PaymentTxService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;

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
        return paymentRepository.save(Payment.approved(order, res));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailed(Long orderId, Long buyerId, String reason) {
        notificationRepository.save(Notification.paymentFailed(buyerId, orderId, reason));
    }
}
