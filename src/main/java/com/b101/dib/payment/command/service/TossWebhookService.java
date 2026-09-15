package com.b101.dib.payment.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.repository.PaymentRepository;
import com.b101.dib.payment.toss.TossPaymentResponse;
import com.b101.dib.payment.toss.TossPaymentsClient;
import com.b101.dib.payment.toss.TossWebhookEvent;
import java.time.Duration;

public interface TossWebhookService {
    void handle(TossWebhookEvent event);
}
