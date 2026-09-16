package com.b101.dib.member.command.dto;

import java.time.LocalDateTime;

import com.b101.dib.member.domain.MemberStatus;

public record WithdrawalResponse(
        /** 탈퇴를 신청한 시각 */
        LocalDateTime requestedAt,

        /** 탈퇴 상태로 전환될 예정 시각 */
        LocalDateTime scheduledAt,

        /** 유예기간이 끝난 뒤 적용될 회원 상태 */
        MemberStatus status
) {
}
