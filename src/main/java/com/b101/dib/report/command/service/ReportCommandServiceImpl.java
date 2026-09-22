package com.b101.dib.report.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.payment.command.service.PaymentCommandService;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.report.command.dto.CreateOrderReportRequest;
import com.b101.dib.report.command.dto.CreateReportRequest;
import com.b101.dib.report.command.dto.ProcessReportRequest;
import com.b101.dib.report.domain.Report;
import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportCommandServiceImpl implements ReportCommandService {
    private final ReportRepository reportRepository;
    private final AuctionRepository auctionRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final PaymentCommandService paymentCommandService;
    private final ReportProcessTxService reportProcessTxService;

    @Override
    public Long reportAuction(Long memberId, Long auctionId, CreateReportRequest request) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        Product product = productRepository.findById(auction.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        if (product.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }
        if (reportRepository.existsByMemberIdAndAuctionId(memberId, auctionId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
                .memberId(memberId)
                .content(request.getContent())
                .type(ReportType.AUCTION)
                .auctionId(auctionId)
                .reportTargetId(product.getMemberId())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return reportRepository.save(report).getReportId();
    }
    
    @Override
    public Long reportOrder(Long memberId, Long orderId, CreateOrderReportRequest request) {
        // 거래 신고는 ORDER/CHATTING만 다룬다. MEMBER가 이 경로로 들어오면 order_id가 붙은 회원 신고가 생겨 중복 기준과 대상 표기가 어긋난다.
        if (request.getType() != ReportType.ORDER && request.getType() != ReportType.CHATTING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        Long targetId;
        if (order.getBuyerId().equals(memberId)) {
            targetId = order.getSellerId();
        } else if (order.getSellerId().equals(memberId)) {
            targetId = order.getBuyerId();
        } else {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (reportRepository.existsByMemberIdAndReportTargetIdAndTypeAndOrderId(memberId, targetId, request.getType(), orderId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
                .memberId(memberId)
                .content(request.getContent())
                .type(request.getType())
                .orderId(orderId)
                .reportTargetId(targetId)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        Long reportId = reportRepository.save(report).getReportId();

        // 신고가 들어온 거래는 관리자 결론이 날 때까지 멈춘다. 자동 구매 확정이 먼저 돌면 환불 경로가 닫힌다.
        // 이미 CONFIRMED/CANCELED/REFUNDED인 주문은 막을 것도 되돌릴 것도 없어 hold()가 그대로 통과시킨다.
        order.hold(reportId);
        if (order.isOnHold()) {
            notificationRepository.save(Notification.system(order.getSellerId(), "거래 보류",
                    "주문 #" + orderId + " 거래가 신고 접수로 보류되었습니다. 관리자 확인 후 안내드립니다."));
            notificationRepository.save(Notification.order(orderId, order.getBuyerId(), "거래 보류",
                    "주문 #" + orderId + " 거래가 신고 접수로 보류되었습니다. 관리자 확인 후 안내드립니다."));
        }
        return reportId;
    }

    @Override
    public Long reportMember(Long memberId, Long targetMemberId, CreateReportRequest request) {
        if (targetMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }
        if (!memberRepository.existsById(targetMemberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        // 같은 대상이라도 이미 처리된 신고는 별개 사안이므로, 아직 미처리(PENDING)인 신고가 있을 때만 중복으로 본다.
        if (reportRepository.existsByMemberIdAndReportTargetIdAndTypeAndStatus(memberId, targetMemberId, ReportType.MEMBER, ReportStatus.PENDING)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
                .memberId(memberId)
                .content(request.getContent())
                .type(ReportType.MEMBER)
                .reportTargetId(targetMemberId)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return reportRepository.save(report).getReportId();
    }
    
    // 환불은 토스 취소(외부 HTTP)라 트랜잭션 안에서 부르면 커넥션을 물고 대기한다. DB 작업은 ReportProcessTxService가 맡는다
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LocalDateTime process(Long reportId, ProcessReportRequest request) {
        if (request.getStatus() == ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        Report report = reportProcessTxService.load(reportId);
        // ACCEPTED는 "인정했으나 결론 미정"이라 보류가 유지된다 — 이후 REFUNDED/REJECTED로 다시 처리해 보류를 풀 수 있어야 한다.
        // REFUNDED/REJECTED는 종결 상태라 재처리하지 않는다(중복 환불 방지).
        if (report.getStatus() != ReportStatus.PENDING && report.getStatus() != ReportStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (request.getStatus() == ReportStatus.REFUNDED) {
            if (report.getOrderId() == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
            Long paymentId = reportProcessTxService.findRefundablePaymentId(report.getOrderId());
            String reason = request.getRefundReason() == null || request.getRefundReason().isBlank()
                    ? "신고 #" + reportId + " 처리에 따른 환불" : request.getRefundReason();
            // 실제 취소가 성공해야 주문이 REFUNDED가 된다. 실패하면 예외가 그대로 올라가 신고 상태는 바뀌지 않는다
            paymentCommandService.refund(paymentId, reason, request.getRefundAmount());
        }

        return reportProcessTxService.finish(reportId, request);
    }
}