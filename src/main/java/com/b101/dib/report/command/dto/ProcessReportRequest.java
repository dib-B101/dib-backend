package com.b101.dib.report.command.dto;

import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.report.domain.ReportStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessReportRequest {
    @NotNull
    private ReportStatus status;

    /** true면 피신고자를 제재한다. REJECTED(기각)일 때는 무시된다. */
    private Boolean sanction;

    /** 제재 시 적용할 회원 상태. 비우면 SUSPENDED(정지). */
    private MemberStatus sanctionStatus;

    /** 제재 시 덮어쓸 누적 경고 수. 비우면 기존 값을 유지한다. */
    @Min(0)
    private Integer warningCount;

    /** status=REFUNDED일 때 토스 취소 사유. 비우면 신고 번호로 기본 사유를 만든다. */
    private String refundReason;

    /** 부분 환불 금액. 비우면 전액 환불. */
    private Long refundAmount;

    /** 처리 사유 메모. 당사자 알림 본문에 덧붙는다. */
    private String adminNote;
}
