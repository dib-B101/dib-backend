package com.b101.dib.report.command.controller;

import com.b101.dib.report.command.dto.ProcessReportRequest;
import com.b101.dib.report.command.service.ReportCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportCommandController {
    private final ReportCommandService reportCommandService;

    @PatchMapping("/{reportId}")
    public ResponseEntity<Map<String, Object>> process(@PathVariable("reportId") Long reportId,
                                                       @Valid @RequestBody ProcessReportRequest request) {
        LocalDateTime processedAt = reportCommandService.process(reportId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "신고 처리 성공");
        map.put("reportId", reportId);
        map.put("status", request.getStatus());
        map.put("processedAt", processedAt);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
