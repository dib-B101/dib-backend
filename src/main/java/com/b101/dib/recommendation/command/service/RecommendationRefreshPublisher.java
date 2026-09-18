package com.b101.dib.recommendation.command.service;

import com.b101.dib.common.ai.AiServerProperties;
import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.common.util.Times;
import com.b101.dib.recommendation.repository.RecommendationCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationRefreshPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RecommendationCache recommendationCache;
    private final AiServerProperties properties;

    public void requestIfNeeded(Long memberId) {
        if (!properties.isEnabled()) {
            return;
        }
        Long normalizedMemberId = memberId == null ? 0L : memberId;
        long bucket = Instant.now().getEpochSecond() / 30;
        String jobId = "home-general-" + normalizedMemberId + "-" + bucket;
        if (!recommendationCache.beginRefresh(normalizedMemberId, jobId)) {
            return;
        }

        EventEnvelope envelope = new EventEnvelope();
        envelope.setEventType("RECOMMENDATION_REQUESTED");
        envelope.setEventId(UUID.randomUUID().toString());
        envelope.setOccurredAt(Times.iso(LocalDateTime.now()));
        envelope.setAggregateType("MEMBER");
        envelope.setAggregateId(normalizedMemberId);
        envelope.setPayload(Map.of(
                "jobId", jobId,
                "memberId", normalizedMemberId,
                "scope", "GENERAL"
        ));
        try {
            kafkaTemplate.send(KafkaTopics.RECOMMENDATION_REQUESTED, normalizedMemberId.toString(), envelope)
                    .whenComplete((result, error) -> {
                        if (error != null) {
                            recommendationCache.endRefresh(normalizedMemberId, jobId);
                            log.warn("추천 요청 이벤트 발행 실패 memberId={}", normalizedMemberId, error);
                        }
                    });
        } catch (Exception e) {
            recommendationCache.endRefresh(normalizedMemberId, jobId);
            log.warn("추천 요청 이벤트 발행 실패 memberId={}", normalizedMemberId, e);
        }
    }

    // 비로그인 홈도 첫 요청부터 AI 스냅샷을 쓸 가능성을 높이기 위한 공용 캐시 예열.
    // 실제 AI 호출은 이 스케줄러가 아니라 Kafka Consumer 에서만 수행한다.
    @Scheduled(
            initialDelayString = "${dib.ai.recommendation-initial-delay-ms:2000}",
            fixedDelayString = "${dib.ai.recommendation-refresh-ms:240000}"
    )
    public void warmAnonymousRecommendation() {
        requestIfNeeded(0L);
    }
}
