package com.b101.dib.report.query.service;

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
    public List<ReportQueryDto> findMine(Long memberId, ReportType type, ReportStatus status) {
        return reportMapper.findByMemberId(memberId, type, status);
    }

    @Override
    public List<AdminReportQueryDto> findAll(ReportType type, ReportStatus status) {
        return reportMapper.findAll(type, status);
    }
}
