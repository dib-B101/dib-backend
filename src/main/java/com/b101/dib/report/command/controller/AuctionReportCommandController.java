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
@RequestMapping("/api/v1/auctions/{auctionId}/reports")
@RequiredArgsConstructor
public class AuctionReportCommandController {
    private final ReportCommandService reportCommandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> reportAuction(@RequestHeader("X-Member-Id") Long memberId,
                                                             @PathVariable("auctionId") Long auctionId,
                                                             @Valid @RequestBody CreateReportRequest request) {
        Long reportId = reportCommandService.reportAuction(memberId, auctionId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "경매 신고 접수");
        map.put("reportId", reportId);
        map.put("status", "PENDING");
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}