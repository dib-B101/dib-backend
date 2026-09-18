package com.b101.dib.recommendation.command.controller;

import com.b101.dib.common.ai.AiServerProperties;
import com.b101.dib.common.ai.HmacSigner;
import com.b101.dib.recommendation.command.dto.RecommendationCallbackRequest;
import com.b101.dib.recommendation.repository.RecommendationCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/v1/ai/callbacks")
@RequiredArgsConstructor
public class RecommendationCallbackController {
    private final RecommendationCache recommendationCache;
    private final AiServerProperties aiServerProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> recommendations(
            @RequestHeader(value = HmacSigner.TIMESTAMP_HEADER, required = false) String timestamp,
            @RequestHeader(value = HmacSigner.SIGNATURE_HEADER, required = false) String signature,
            @RequestBody byte[] body) {
        HashMap<String, Object> response = new HashMap<>();
        if (!HmacSigner.verify(aiServerProperties.getAiHmacSecret(), timestamp, signature, body,
                aiServerProperties.getMaxSkewSeconds())) {
            response.put("message", "서명이 올바르지 않습니다");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        RecommendationCallbackRequest request = objectMapper.readValue(body, RecommendationCallbackRequest.class);
        boolean saved = recommendationCache.saveIfCurrent(request);
        response.put("message", saved ? "AI 추천 결과 저장" : "더 최신 추천 요청이 있어 결과를 무시했습니다");
        response.put("data", Map.of(
                "jobId", request.getJobId(),
                "memberId", request.getMemberId(),
                "saved", saved,
                "itemCount", request.getItems().size()
        ));
        return ResponseEntity.ok(response);
    }
}
