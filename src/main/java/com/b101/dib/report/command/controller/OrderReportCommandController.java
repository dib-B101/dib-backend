package com.b101.dib.report.command.controller;

import com.b101.dib.report.command.dto.CreateOrderReportRequest;
import com.b101.dib.report.command.service.ReportCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/reports")
@RequiredArgsConstructor
public class OrderReportCommandController {
    private final ReportCommandService reportCommandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> reportOrder(@RequestHeader("X-Member-Id") Long memberId,
                                                           @PathVariable("orderId") Long orderId,
                                                           @Valid @RequestBody CreateOrderReportRequest request) {
        Long reportId = reportCommandService.reportOrder(memberId, orderId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "거래 신고 접수");
        map.put("reportId", reportId);
        map.put("status", "PENDING");
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}