package com.b101.dib.report.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.SanctionMemberRequest;
import com.b101.dib.member.command.service.MemberCommandService;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.payment.domain.Payment;
import com.b101.dib.payment.repository.PaymentRepository;
import com.b101.dib.report.command.dto.ProcessReportRequest;
import com.b101.dib.report.domain.Report;
import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportProcessTxServiceImpl implements ReportProcessTxService {
    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final MemberCommandService memberCommandService;

    @Override
    @Transactional(readOnly = true)
    public Report load(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Long findRefundablePaymentId(Long orderId) {
        Payment payment = paymentRepository.findFirstByOrderIdOrderByPaymentIdDesc(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return payment.getPaymentId();
    }

    @Override
    public LocalDateTime finish(Long reportId, ProcessReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
        ReportStatus status = request.getStatus();
        LocalDateTime now = LocalDateTime.now();
        report.setStatus(status);
        report.setProcessedAt(now);

        boolean sanctioned = sanctionIfRequested(report, request, status);
        String note = request.getAdminNote() == null || request.getAdminNote().isBlank()
                ? "" : " 처리 사유: " + request.getAdminNote();

        if (report.getOrderId() != null) {
            settleHold(report, status);
        }
        notifyParties(report, status, sanctioned, note, report.getOrderId() != null);
        log.info("신고 처리 완료 reportId={} status={} sanctioned={}", reportId, status, sanctioned);
        return now;
    }

    // 제재 로직은 MemberCommandService에만 두고 여기서 재사용한다 — 관리자가 제재 API를 따로 부르지 않아도 되게
    private boolean sanctionIfRequested(Report report, ProcessReportRequest request, ReportStatus status) {
        if (status == ReportStatus.REJECTED || !Boolean.TRUE.equals(request.getSanction())) {
            return false;
        }
        MemberStatus target = request.getSanctionStatus() == null ? MemberStatus.SUSPENDED : request.getSanctionStatus();
        memberCommandService.sanction(report.getReportTargetId(), SanctionMemberRequest.builder()
                .status(target)
                .warningCount(request.getWarningCount())
                .reason("신고 #" + report.getReportId() + " 처리")
                .build());
        return true;
    }

    private void settleHold(Report report, ReportStatus status) {
        Order order = orderRepository.findById(report.getOrderId()).orElse(null);
        if (order == null) {
            return;
        }
        // 인정(ACCEPTED)은 아직 결론이 안 난 상태 — 환불/기각이 정해질 때까지 보류를 유지한다.
        // 기각은 거래 재개, 환불은 주문이 이미 REFUNDED라 더 막을 게 없으므로 둘 다 해제한다.
        if (status == ReportStatus.REJECTED || status == ReportStatus.REFUNDED) {
            order.releaseHold();
        }
    }

    private void notifyParties(Report report, ReportStatus status, boolean sanctioned, String note, boolean orderReport) {
        Long reporter = report.getMemberId();
        Long target = report.getReportTargetId();
        String ref = report.getOrderId() != null ? "주문 #" + report.getOrderId() : "신고 #" + report.getReportId();

        switch (status) {
            case REJECTED -> {
                notificationRepository.save(Notification.system(reporter, "신고 처리 결과",
                        ref + " 신고는 확인 결과 조치 대상이 아니어서 기각되었습니다." + note));
                if (orderReport) {
                    notificationRepository.save(Notification.system(target, "거래 재개",
                            ref + " 관련 신고가 기각되어 보류가 해제되었습니다. 거래를 계속 진행해 주세요." + note));
                    notificationRepository.save(Notification.system(reporter, "거래 재개",
                            ref + " 보류가 해제되었습니다. 거래를 계속 진행해 주세요."));
                }
            }
            case REFUNDED -> {
                notificationRepository.save(Notification.system(reporter, "신고 처리 결과",
                        ref + " 신고가 인정되어 결제 취소·환불 처리되었습니다." + note));
                if (target != null) {
                    notificationRepository.save(Notification.system(target, "거래 환불",
                            ref + " 거래가 신고 처리로 환불되었습니다." + sanctionTail(sanctioned) + note));
                }
            }
            default -> {
                notificationRepository.save(Notification.system(reporter, "신고 처리 결과",
                        ref + " 신고가 인정되었습니다. 환불이 필요한 경우 추가 안내드립니다." + note));
                if (target != null) {
                    notificationRepository.save(Notification.system(target, "신고 접수 결과",
                            ref + " 관련 신고가 인정되었습니다." + sanctionTail(sanctioned) + note));
                }
            }
        }
    }

    private String sanctionTail(boolean sanctioned) {
        return sanctioned ? " 계정에 제재가 적용되었습니다." : "";
    }
}
