package com.b101.dib.member.command.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSettlementAccountRequest {
    // phoneVerificationToken 은 받지 않는다. 가입 때 본인인증을 마친 계정이라 정산 계좌에서 다시 묻지 않는다.
    // 구버전 앱이 계속 보내도 무시되도록 필드를 지우기만 하고 요청 거부는 하지 않는다
    @NotBlank
    private String bankName;
    @NotBlank
    @Pattern(regexp = "^[0-9-]{8,30}$")
    private String accountNumber;
    @NotBlank
    private String accountHolder;
}
