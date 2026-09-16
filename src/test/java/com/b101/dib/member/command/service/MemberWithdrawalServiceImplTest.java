package com.b101.dib.member.command.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import com.b101.dib.auction.repository.AuctionRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.command.dto.WithdrawalRequest;
import com.b101.dib.member.command.dto.WithdrawalResponse;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-09-16T03:00:00Z");

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AuctionRepository auctionRepository;

    private MemberWithdrawalServiceImpl memberWithdrawalService;

    @BeforeEach
    void setUp() {
        memberWithdrawalService = new MemberWithdrawalServiceImpl(
                memberRepository,
                orderRepository,
                auctionRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void schedulesWithdrawalAfterSevenDayGracePeriod() {
        Member member = activeMember(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.existsActiveByMemberId(eq(1L), anySet()))
                .willReturn(false);
        given(auctionRepository.existsActiveByMemberId(eq(1L), anySet()))
                .willReturn(false);

        WithdrawalResponse result = memberWithdrawalService.request(
                1L,
                new WithdrawalRequest("서비스 이용 종료")
        );

        assertThat(result).isEqualTo(new WithdrawalResponse(
                LocalDateTime.of(2026, 9, 16, 3, 0),
                LocalDateTime.of(2026, 9, 23, 3, 0),
                MemberStatus.WITHDRAWN
        ));
        assertThat(member.getDeletedAt()).isEqualTo(result.scheduledAt());
        assertThat(member.getUpdatedAt()).isEqualTo(result.requestedAt());
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void rejectsWithdrawalWhenActiveOrderExists() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember(1L)));
        given(orderRepository.existsActiveByMemberId(eq(1L), anySet()))
                .willReturn(true);

        assertThatThrownBy(() -> memberWithdrawalService.request(
                1L,
                new WithdrawalRequest(null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.ACTIVE_ORDER_EXISTS)
        );

        verify(auctionRepository, never()).existsActiveByMemberId(
                eq(1L),
                anySet()
        );
    }

    @Test
    void rejectsWithdrawalWhenActiveAuctionOrBidExists() {
        given(memberRepository.findById(1L)).willReturn(Optional.of(activeMember(1L)));
        given(orderRepository.existsActiveByMemberId(eq(1L), anySet()))
                .willReturn(false);
        given(auctionRepository.existsActiveByMemberId(eq(1L), anySet()))
                .willReturn(true);

        assertThatThrownBy(() -> memberWithdrawalService.request(
                1L,
                new WithdrawalRequest(null)
        )).isInstanceOfSatisfying(
                BusinessException.class,
                exception -> assertThat(exception.getErrorCode())
                        .isEqualTo(ErrorCode.ACTIVE_AUCTION_EXISTS)
        );
    }

    @Test
    void returnsExistingScheduleForRepeatedRequest() {
        Member member = activeMember(1L);
        member.setDeletedAt(LocalDateTime.of(2026, 9, 23, 3, 0));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        WithdrawalResponse result = memberWithdrawalService.request(
                1L,
                new WithdrawalRequest(null)
        );

        assertThat(result.requestedAt()).isEqualTo(LocalDateTime.of(2026, 9, 16, 3, 0));
        assertThat(result.scheduledAt()).isEqualTo(member.getDeletedAt());
        verify(orderRepository, never()).existsActiveByMemberId(
                eq(1L),
                anySet()
        );
    }

    @Test
    void completesWithdrawalsWhoseGracePeriodExpired() {
        given(memberRepository.completeDueWithdrawals(
                LocalDateTime.of(2026, 9, 16, 3, 0)
        )).willReturn(3);

        int result = memberWithdrawalService.completeDueWithdrawals();

        assertThat(result).isEqualTo(3);
    }

    private Member activeMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .status(MemberStatus.ACTIVE)
                .build();
    }
}
