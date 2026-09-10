package com.b101.dib.report.command.controller;

import com.b101.dib.report.command.dto.CreateReportRequest;
import com.b101.dib.report.command.service.ReportCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/{targetMemberId}/reports")
@RequiredArgsConstructor
public class MemberReportCommandController {
    private final ReportCommandService reportCommandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> reportMember(@RequestHeader("X-Member-Id") Long memberId,
                                                            @PathVariable("targetMemberId") Long targetMemberId,
                                                            @Valid @RequestBody CreateReportRequest request) {
        Long reportId = reportCommandService.reportMember(memberId, targetMemberId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "회원 신고 접수");
        map.put("reportId", reportId);
        map.put("status", "PENDING");
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}