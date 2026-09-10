package com.b101.dib.member.command.dto;

import com.b101.dib.member.domain.MemberStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SanctionMemberRequest {
    @Min(0)
    private Integer warningCount;

    @NotNull
    private MemberStatus status;

    private String reason;
}
