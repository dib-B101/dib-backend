package com.b101.dib.member.query.dto;

import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminMemberQueryDto {
    private Long memberId;
    private String email;
    private String nickname;
    private String name;
    private MemberStatus status;
    private MemberRole role;
    private Double score;
    private Integer warningCount;
    private LocalDateTime suspendedAt;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
