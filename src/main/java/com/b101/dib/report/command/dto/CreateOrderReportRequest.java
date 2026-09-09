package com.b101.dib.report.command.dto;

import com.b101.dib.report.domain.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderReportRequest {
    @NotBlank
    private String content;

    @NotNull
    private ReportType type;
}