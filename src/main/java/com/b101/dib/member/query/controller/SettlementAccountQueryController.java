package com.b101.dib.member.query.controller;

import com.b101.dib.member.query.service.SettlementAccountQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/me/settlement-account")
@RequiredArgsConstructor
public class SettlementAccountQueryController {
    private final SettlementAccountQueryService settlementAccountQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(@RequestHeader("X-Member-Id") Long memberId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "정산 계좌 조회 성공");
        map.put("data", settlementAccountQueryService.findMine(memberId));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
