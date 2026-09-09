package com.b101.dib.report.command.service;

import com.b101.dib.report.command.dto.CreateOrderReportRequest;
import com.b101.dib.report.command.dto.CreateReportRequest;

public interface ReportCommandService {
    Long reportAuction(Long memberId, Long auctionId, CreateReportRequest request);
    Long reportOrder(Long memberId, Long orderId, CreateOrderReportRequest request);
    Long reportMember(Long memberId, Long targetMemberId, CreateReportRequest request);
}