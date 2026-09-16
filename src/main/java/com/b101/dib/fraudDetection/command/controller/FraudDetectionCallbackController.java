package com.b101.dib.fraudDetection.command.controller;

import com.b101.dib.common.ai.AiServerProperties;
import com.b101.dib.common.ai.HmacSigner;
import com.b101.dib.fraudDetection.command.dto.BidAnomalyCallbackRequest;
import com.b101.dib.fraudDetection.command.service.FraudDetectionCommandService;
import com.b101.dib.fraudDetection.domain.FraudDetection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

// 명세 93: AI → 백엔드 이상입찰 결과 콜백. AI_HMAC 서명 검증 후 fraud_detection 저장
@RestController
@RequestMapping("/internal/v1/ai/callbacks")
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionCallbackController {
    private final FraudDetectionCommandService fraudDetectionCommandService;
    private final AiServerProperties aiServerProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/bid-anomalies")
    public ResponseEntity<Map<String, Object>> bidAnomalies(
            @RequestHeader(value = HmacSigner.TIMESTAMP_HEADER, required = false) String timestamp,
            @RequestHeader(value = HmacSigner.SIGNATURE_HEADER, required = false) String signature,
            @RequestBody byte[] body) {
        HashMap<String, Object> map = new HashMap<>();
        if (!HmacSigner.verify(aiServerProperties.getAiHmacSecret(), timestamp, signature, body, aiServerProperties.getMaxSkewSeconds())) {
            map.put("message", "서명이 올바르지 않습니다");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
        }
        BidAnomalyCallbackRequest request = objectMapper.readValue(body, BidAnomalyCallbackRequest.class);
        FraudDetection saved = fraudDetectionCommandService.saveCallback(request);
        Map<String, Object> data = new HashMap<>();
        data.put("jobId", request.getJobId());
        data.put("detectionId", saved == null ? null : saved.getDetectionId());
        data.put("duplicated", saved == null);
        map.put("message", saved == null ? "이미 저장된 분석 결과" : "이상입찰 분석 결과 저장");
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
