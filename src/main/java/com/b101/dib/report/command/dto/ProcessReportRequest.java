package com.b101.dib.report.command.dto;

import com.b101.dib.report.domain.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProcessReportRequest {
    @NotNull
    private ReportStatus status;
}