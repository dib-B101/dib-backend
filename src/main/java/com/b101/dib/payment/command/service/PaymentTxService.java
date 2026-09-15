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

public interface PaymentTxService {
    Payment recordApproved(Long orderId, TossPaymentResponse res);

    Payment recordRefunded(Long paymentId, String transactionKey);

    void recordFailed(Long orderId, Long buyerId, String reason);
}
