package com.b101.dib.report.query.dto;

import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ReportQueryDto {
    private Long reportId;
    private String content;
    private ReportType type;
    private Long auctionId;
    private Long orderId;
    private Long reportTargetId;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}