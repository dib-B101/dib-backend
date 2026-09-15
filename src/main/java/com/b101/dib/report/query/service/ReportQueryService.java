package com.b101.dib.report.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.query.dto.AdminReportQueryDto;
import com.b101.dib.report.query.dto.ReportQueryDto;


public interface ReportQueryService {
    CursorPageDto<ReportQueryDto> findMine(Long memberId, ReportType type, ReportStatus status, String cursor, int size);
    CursorPageDto<AdminReportQueryDto> findAll(ReportType type, ReportStatus status, String cursor, int size);
}
