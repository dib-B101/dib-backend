package com.b101.dib.fraudDetection.query.controller;

import com.b101.dib.fraudDetection.query.dto.FraudDetectionQueryDto;
import com.b101.dib.fraudDetection.query.service.FraudDetectionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/fraud-detections")
@RequiredArgsConstructor
public class AdminFraudDetectionQueryController {
    private final FraudDetectionQueryService fraudDetectionQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(@RequestParam(name = "auctionId", required = false) Long auctionId,
                                                       @RequestParam(name = "memberId", required = false) Long memberId,
                                                       @RequestParam(name = "riskScoreGte", required = false) Double riskScoreGte) {
        List<FraudDetectionQueryDto> dtoList = fraudDetectionQueryService.findAll(auctionId, memberId, riskScoreGte);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "이상입찰 탐지 결과 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
