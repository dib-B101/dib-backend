package com.b101.dib.report.command.service;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import com.b101.dib.product.repository.ProductRepository;
import com.b101.dib.product.domain.Product;
import com.b101.dib.report.command.dto.CreateOrderReportRequest;
import com.b101.dib.report.command.dto.CreateReportRequest;
import com.b101.dib.report.domain.Report;
import com.b101.dib.report.domain.ReportStatus;
import com.b101.dib.report.domain.ReportType;
import com.b101.dib.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
        if (request.getType() == ReportType.AUCTION) {
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
        return reportRepository.save(report).getReportId();
    }

    @Override
    public Long reportMember(Long memberId, Long targetMemberId, CreateReportRequest request) {
        if (targetMemberId.equals(memberId)) {
            throw new BusinessException(ErrorCode.SELF_REPORT_NOT_ALLOWED);
        }
        if (!memberRepository.existsById(targetMemberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        if (reportRepository.existsByMemberIdAndReportTargetIdAndTypeAndOrderId(memberId, targetMemberId, ReportType.CHATTING, null)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REPORT);
        }

        Report report = Report.builder()
                .memberId(memberId)
                .content(request.getContent())
                .type(ReportType.CHATTING)
                .reportTargetId(targetMemberId)
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        return reportRepository.save(report).getReportId();
    }
}