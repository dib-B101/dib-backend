package com.b101.dib.report.command.service;

import com.b101.dib.report.command.dto.ProcessReportRequest;
import com.b101.dib.report.domain.Report;

import java.time.LocalDateTime;

// 신고 처리 중 DB 작업만 담당한다. 환불은 외부 HTTP(토스 취소)라 트랜잭션 밖에서 불러야 하므로
// PaymentCommandService/PaymentTxService와 같은 방식으로 오케스트레이션과 트랜잭션을 분리한다
public interface ReportProcessTxService {
    Report load(Long reportId);

    Long findRefundablePaymentId(Long orderId);

    LocalDateTime finish(Long reportId, ProcessReportRequest request);
}
