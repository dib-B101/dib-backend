package com.b101.dib.member.query.service;

import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;
import com.b101.dib.member.query.dto.MemberDetailDto;

import java.util.List;

public interface MemberQueryService {
    List<AdminMemberQueryDto> findAll(String q, MemberStatus status, Integer warningCount);

    MemberDetailDto findMine(Long memberId);
}
