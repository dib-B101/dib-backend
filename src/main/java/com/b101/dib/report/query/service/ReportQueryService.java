package com.b101.dib.report.query.service;

import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.query.dto.ReportQueryDto;

import java.util.List;

public interface ReportQueryService {
    List<ReportQueryDto> findMine(Long memberId, ReportType type, ReportStatus status);
}