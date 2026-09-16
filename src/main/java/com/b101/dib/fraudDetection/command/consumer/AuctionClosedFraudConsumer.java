package com.b101.dib.fraudDetection.command.consumer;

import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.consumedEvent.command.service.ConsumedEventService;
import com.b101.dib.fraudDetection.command.service.FraudDetectionCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

// dib.auction.closed → AI 서버에 입찰자별 이상입찰 분석 요청 (결과는 콜백으로)
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionClosedFraudConsumer {
    private final FraudDetectionCommandService fraudDetectionCommandService;
    private final ConsumedEventService consumedEventService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.AUCTION_CLOSED, groupId = KafkaTopics.GROUP_FRAUD)
    public void onAuctionClosed(String message) {
        EventEnvelope e = objectMapper.readValue(message, EventEnvelope.class);
        if (!"SOLD".equals(e.getString("result"))) {
            return;   // 입찰 없는 유찰은 분석할 것이 없다
        }
        if (!consumedEventService.claimInNewTransaction(KafkaTopics.GROUP_FRAUD, e.getEventId())) {
            return;
        }
        int sent = fraudDetectionCommandService.requestAnalysis(e.getLong("auctionId"));
        log.info("이상입찰 분석 요청 auctionId={} {}건", e.getLong("auctionId"), sent);
    }
}
