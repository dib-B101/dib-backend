package com.b101.dib.report.repository;

import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.query.dto.AdminReportQueryDto;
import com.b101.dib.report.query.dto.ReportQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {
    List<ReportQueryDto> findByMemberId(@Param("memberId") Long memberId,
                                        @Param("type") ReportType type,
                                        @Param("status") ReportStatus status);
    List<AdminReportQueryDto> findAll(@Param("type") ReportType type,
            @Param("status") ReportStatus status);
}