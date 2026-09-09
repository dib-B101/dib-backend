package com.b101.dib.report.query.controller;

import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.query.dto.AdminReportQueryDto;
import com.b101.dib.report.query.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class AdminReportQueryController {
    private final ReportQueryService reportQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(@RequestParam(name = "type", required = false) ReportType type,
                                                       @RequestParam(name = "status", required = false) ReportStatus status) {
        List<AdminReportQueryDto> dtoList = reportQueryService.findAll(type, status);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "신고 전체 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
