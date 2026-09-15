package com.b101.dib.settlement.command.controller;

import com.b101.dib.settlement.command.service.SettlementCommandService;
import com.b101.dib.settlement.domain.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal/settlements")
@RequiredArgsConstructor
public class InternalSettlementController {
    private final SettlementCommandService settlementCommandService;

    @PostMapping("/{settlementId}/execute")
    public ResponseEntity<Map<String, Object>> execute(@PathVariable("settlementId") Long settlementId) {
        Settlement settlement = settlementCommandService.execute(settlementId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "정산 지급 완료");
        map.put("data", settlement);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(map);
    }
}
