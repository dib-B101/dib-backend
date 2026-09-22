package com.b101.dib.outboxEvent.command.service;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

// outbox_event 를 짧은 주기로 폴링해 Kafka 로 보낸다. 한 번에 다 못 보내면 다음 주기로
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxPublishTxService outboxPublishTxService;
    private final MeterRegistry meterRegistry;

    // 발행을 포기한 행 수. 게이지로 올려 두면 /actuator/prometheus 로 "이벤트가 몇 건 유실됐나" 를 바로 본다
    private final AtomicLong abandoned = new AtomicLong();

    @Value("${dib.outbox.batch-size:100}")
    private int batchSize;

    @PostConstruct
    void registerMetrics() {
        meterRegistry.gauge("dib.outbox.abandoned", abandoned);
    }

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

    // 포기한 행은 재시도 대상에서 빠지므로 로그를 안 보면 사라진 줄도 모른다. 주기적으로 알린다
    @Scheduled(fixedDelayString = "${dib.outbox.abandoned-check-ms:300000}")
    public void reportAbandoned() {
        try {
            long count = outboxPublishTxService.countAbandoned();
            abandoned.set(count);
            if (count > 0) {
                log.error("outbox 발행을 포기한 이벤트 {}건 — 원인을 고치고 attempts 를 0 으로 되돌려야 재발행된다", count);
            }
        } catch (Exception e) {
            log.warn("outbox 포기 건수 확인 실패", e);
        }
    }

    // 발행 완료 행을 안 지우면 테이블이 무한히 커진다. Pod 마다 돌 필요는 없으므로 ShedLock 으로 한 대만
    @Scheduled(cron = "${dib.outbox.purge-cron:0 20 4 * * *}")
    @SchedulerLock(name = "outboxPurge", lockAtLeastFor = "PT1M", lockAtMostFor = "PT10M")
    public void purge() {
        try {
            int deleted = outboxPublishTxService.purgePublished();
            if (deleted > 0) {
                log.info("outbox 발행 완료 행 정리 {}건", deleted);
            }
        } catch (Exception e) {
            log.warn("outbox 정리 실패", e);
        }
    }
}
