package com.b101.dib.recommendation.command.consumer;

import com.b101.dib.common.messaging.EventEnvelope;
import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.recommendation.command.service.RecommendationCommandService;
import com.b101.dib.recommendation.repository.RecommendationCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationRequestedConsumer {
    private final RecommendationCommandService recommendationCommandService;
    private final RecommendationCache recommendationCache;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.RECOMMENDATION_REQUESTED, groupId = KafkaTopics.GROUP_RECOMMENDATION)
    public void onRecommendationRequested(String message) {
        EventEnvelope event = objectMapper.readValue(message, EventEnvelope.class);
        String jobId = event.getString("jobId");
        Long memberId = event.getLong("memberId");
        String scope = event.getString("scope");
        boolean accepted = recommendationCommandService.request(jobId, memberId, scope == null ? "GENERAL" : scope);
        if (!accepted) {
            recommendationCache.endRefresh(memberId, jobId);
        }
        log.info("AI 추천 요청 memberId={} jobId={} accepted={}", memberId, jobId, accepted);
    }
}
