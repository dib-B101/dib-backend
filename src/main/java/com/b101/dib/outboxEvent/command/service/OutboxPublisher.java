package com.b101.dib.outboxEvent.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// outbox_event 를 짧은 주기로 폴링해 Kafka 로 보낸다. 한 번에 다 못 보내면 다음 주기로
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxPublishTxService outboxPublishTxService;

    @Value("${dib.outbox.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${dib.outbox.poll-ms:500}")
    public void run() {
        try {
            int n = outboxPublishTxService.publishBatch(batchSize);
            if (n > 0) {
                log.debug("outbox 발행 {}건", n);
            }
        } catch (Exception e) {
            log.error("outbox 폴링 실패", e);
        }
    }
}
