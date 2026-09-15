package com.b101.dib.report.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.query.dto.AdminReportQueryDto;
import com.b101.dib.report.query.dto.ReportQueryDto;
import com.b101.dib.report.repository.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportQueryServiceImpl implements ReportQueryService {
    private final ReportMapper reportMapper;

    @Override
    public CursorPageDto<ReportQueryDto> findMine(Long memberId, ReportType type, ReportStatus status, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<ReportQueryDto> rows = reportMapper.findByMemberId(memberId, type, status, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, ReportQueryDto::getReportId);
    }

    @Override
    public CursorPageDto<AdminReportQueryDto> findAll(ReportType type, ReportStatus status, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<AdminReportQueryDto> rows = reportMapper.findAll(type, status, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, AdminReportQueryDto::getReportId);
    }
}
