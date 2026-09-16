package com.b101.dib.member.command.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Set;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.WithdrawalRequest;
import com.b101.dib.member.command.dto.WithdrawalResponse;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberWithdrawalServiceImpl implements MemberWithdrawalService {

    private static final int WITHDRAWAL_GRACE_DAYS = 7;
    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES = EnumSet.of(
            OrderStatus.PENDING,
            OrderStatus.PAID,
            OrderStatus.PREPARING,
            OrderStatus.SHIPPED,
            OrderStatus.DELIEVERED
    );
    private static final Set<AuctionStatus> ACTIVE_AUCTION_STATUSES = EnumSet.of(
            AuctionStatus.SCHEDULED,
//            AuctionStatus.DEPOSITED,
            AuctionStatus.ACTIVE
    );

    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final AuctionRepository auctionRepository;
    private final Clock clock;

    @Override
    public WithdrawalResponse request(Long memberId, WithdrawalRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getDeletedAt() != null) {
            return response(member.getDeletedAt());
        }
        if (orderRepository.existsActiveByMemberId(memberId, ACTIVE_ORDER_STATUSES)) {
            throw new BusinessException(ErrorCode.ACTIVE_ORDER_EXISTS);
        }
        if (auctionRepository.existsActiveByMemberId(memberId, ACTIVE_AUCTION_STATUSES)) {
            throw new BusinessException(ErrorCode.ACTIVE_AUCTION_EXISTS);
        }

        LocalDateTime requestedAt = now();
        LocalDateTime scheduledAt = requestedAt.plusDays(WITHDRAWAL_GRACE_DAYS);
        member.scheduleWithdrawal(scheduledAt, requestedAt);

        return new WithdrawalResponse(requestedAt, scheduledAt, MemberStatus.WITHDRAWN);
    }

    @Override
    public int completeDueWithdrawals() {
        LocalDateTime now = now();
        return memberRepository.completeDueWithdrawals(now);
    }

    private WithdrawalResponse response(LocalDateTime scheduledAt) {
        return new WithdrawalResponse(
                scheduledAt.minusDays(WITHDRAWAL_GRACE_DAYS),
                scheduledAt,
                MemberStatus.WITHDRAWN
        );
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
