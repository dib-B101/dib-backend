package com.b101.dib.recommendation.command.service;

import com.b101.dib.common.ai.AiServerClient;
import com.b101.dib.recommendation.command.dto.RecommendationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationCommandServiceImpl implements RecommendationCommandService {
    public static final String CALLBACK_PATH = "/internal/v1/ai/callbacks/recommendations";

    private final AiServerClient aiServerClient;

    @Override
    public boolean request(String jobId, Long memberId, String scope) {
        if (!aiServerClient.isEnabled()) {
            log.debug("AI 비활성 — 추천 요청 생략 memberId={}", memberId);
            return false;
        }
        RecommendationRequest request = RecommendationRequest.builder()
                .jobId(jobId)
                .memberId(memberId == null ? 0L : memberId)
                .scope(scope)
                .candidateAuctionIds(List.of())
                .callbackUrl(aiServerClient.callbackUrl(CALLBACK_PATH))
                .build();
        return aiServerClient.postAccepted(
                AiServerClient.RECOMMENDATIONS_PATH, request, "홈 추천 " + jobId);
    }
}
