package com.b101.dib.settlement.query.controller;

import com.b101.dib.settlement.query.service.SettlementQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementQueryController {
    private final SettlementQueryService settlementQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(@RequestHeader("X-Member-Id") Long memberId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "정산 목록 조회 성공");
        map.put("data", settlementQueryService.findMine(memberId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<Map<String, Object>> findDetail(@RequestHeader("X-Member-Id") Long memberId,
                                                          @PathVariable("settlementId") Long settlementId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "정산 상세 조회 성공");
        map.put("data", settlementQueryService.findDetail(memberId, settlementId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
