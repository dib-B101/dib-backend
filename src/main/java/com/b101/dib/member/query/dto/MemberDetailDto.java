package com.b101.dib.member.query.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.b101.dib.member.domain.Gender;
import com.b101.dib.member.domain.MemberRole;
import com.b101.dib.member.domain.MemberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberDetailDto {

    private Long memberId;
    private String email;
    private String name;
    private String phoneNumber;
    private String nickname;
    private Gender gender;
    private LocalDate birthDate;
    private MemberStatus status;
    private MemberRole role;
    private Double score;
    private LocalDateTime lastLoginAt;
    private String bankName;
    private String accountHolder;
    private String maskedAccountNumber;
}
