package com.b101.dib.member.command.controller;

import com.b101.dib.member.command.dto.UpdateSettlementAccountRequest;
import com.b101.dib.member.command.service.SettlementAccountCommandService;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.query.dto.SettlementAccountDetailDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/me/settlement-account")
@RequiredArgsConstructor
public class SettlementAccountCommandController {
    private final SettlementAccountCommandService settlementAccountCommandService;

    @PutMapping
    public ResponseEntity<Map<String, Object>> update(@RequestHeader("X-Member-Id") Long memberId,
                                                      @RequestBody @Valid UpdateSettlementAccountRequest request) {
        Member member = settlementAccountCommandService.update(memberId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "정산 계좌 등록 성공");
        map.put("settlementAccount", SettlementAccountDetailDto.from(member));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
