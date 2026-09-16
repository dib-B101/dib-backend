package com.b101.dib.member.command.dto;

public record WithdrawalRequest(
        /** 탈퇴 사유. 별도 탈퇴 테이블을 사용하지 않으므로 저장하지 않는다. */
        String reason
) {
}
