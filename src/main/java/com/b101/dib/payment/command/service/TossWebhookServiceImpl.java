package com.b101.dib.payment.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.repository.PaymentRepository;
import com.b101.dib.payment.toss.TossPaymentResponse;
import com.b101.dib.payment.toss.TossPaymentsClient;
import com.b101.dib.payment.toss.TossWebhookEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TossWebhookServiceImpl implements TossWebhookService {   // 외부 HTTP를 부르므로 @Transactional 없음
    private static final String EVENT_PAYMENT_STATUS = "PAYMENT_STATUS_CHANGED";
    private static final Duration DEDUPE_TTL = Duration.ofDays(30);

    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentRepository paymentRepository;
    private final PaymentTxService paymentTxService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void handle(TossWebhookEvent event) {
        if (event == null || event.data() == null || event.data().paymentKey() == null || event.eventType() == null) {
            throw new BusinessException(ErrorCode.INVALID_EVENT);
        }
        if (!EVENT_PAYMENT_STATUS.equals(event.eventType())) {
            log.info("웹훅 무시 eventType={}", event.eventType());
            return;
        }
        String key = "webhook:toss:" + event.data().paymentKey() + ":" + event.data().status();
        Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", DEDUPE_TTL);
        if (Boolean.FALSE.equals(first)) {
            log.info("웹훅 중복 수신 무시 key={}", key);
            return;
        }
        TossPaymentResponse res = tossPaymentsClient.findByPaymentKey(event.data().paymentKey());
        switch (res.status()) {
            case "DONE" -> onDone(res);
            case "CANCELED", "PARTIAL_CANCELED" -> onCanceled(res);
            default -> log.info("웹훅 상태 처리 없음 paymentKey={} status={}", res.paymentKey(), res.status());
        }
    }

    private void onDone(TossPaymentResponse res) {
        if (paymentRepository.existsByPaymentKey(res.paymentKey())) {
            return;
        }
        Long orderId = parseOrderId(res.orderId());
        if (orderId == null) {
            log.warn("웹훅 orderId 해석 실패 tossOrderId={}", res.orderId());
            return;
        }
        try {
            paymentTxService.recordApproved(orderId, res);
            log.warn("[결제 정합성] 웹훅으로 누락 승인 복구 orderId={} paymentKey={}", orderId, res.paymentKey());
        } catch (BusinessException e) {
            log.error("[결제 정합성] 웹훅 승인 반영 실패 orderId={} paymentKey={} code={}", orderId, res.paymentKey(), e.getErrorCode());
        }
    }

    private void onCanceled(TossPaymentResponse res) {
        Payment payment = paymentRepository.findByPaymentKey(res.paymentKey()).orElse(null);
        if (payment == null || payment.isRefunded()) {
            return;
        }
        try {
            paymentTxService.recordRefunded(payment.getPaymentId(), res.lastTransactionKey());
            log.warn("[결제 정합성] 웹훅으로 외부 취소 반영 paymentId={}", payment.getPaymentId());
        } catch (BusinessException e) {
            log.error("[결제 정합성] 웹훅 취소 반영 실패 paymentId={} code={}", payment.getPaymentId(), e.getErrorCode());
        }
    }

    // 우리가 보낸 orderId 형식: dib-{orderId}-{millis}
    static Long parseOrderId(String tossOrderId) {
        if (tossOrderId == null || !tossOrderId.startsWith("dib-")) {
            return null;
        }
        String[] parts = tossOrderId.split("-");
        if (parts.length < 2) {
            return null;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
