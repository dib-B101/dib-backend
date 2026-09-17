package com.b101.dib.payment.command.consumer;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.consumedEvent.command.service.ConsumedEventService;
import com.b101.dib.payment.command.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// dib.auction.closed → 낙찰 주문 자동결제 (토스 빌링). 외부 API 라 스케줄러·종료 트랜잭션 밖에서 돈다
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionClosedPaymentConsumer {
    private final PaymentCommandService paymentCommandService;
    private final ConsumedEventService consumedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.AUCTION_CLOSED, groupId = KafkaTopics.GROUP_PAYMENT)
    public void onAuctionClosed(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        Long orderId = e.getLong("orderId");
        if (!"SOLD".equals(e.getString("result")) || orderId == null) {
            return;
        }
        if (!consumedEventService.claimInNewTransaction(KafkaTopics.GROUP_PAYMENT, e.getEventId())) {
            return;
        }
        try {
            paymentCommandService.autoCharge(orderId);   // 결제 자체는 autoCharge 안에서 PaymentTxService 로 기록
        } catch (BusinessException ex) {
            log.warn("낙찰 자동결제 실패 orderId={} code={} — 주문은 PENDING, 구매자가 재시도", orderId, ex.getErrorCode());
        } catch (RuntimeException ex) {
            log.warn("낙찰 자동결제 실패 orderId={} — {}", orderId, ex.getMessage());
        }
    }
}
