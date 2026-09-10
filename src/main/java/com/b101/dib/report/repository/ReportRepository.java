package com.b101.dib.report.repository;

import com.b101.dib.report.domain.Report;
import com.b101.dib.report.domain.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
    boolean existsByMemberIdAndAuctionId(Long memberId, Long auctionId);
    boolean existsByMemberIdAndReportTargetIdAndTypeAndOrderId(Long memberId, Long reportTargetId, ReportType type, Long orderId);
}