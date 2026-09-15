package com.b101.dib.member.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;
import com.b101.dib.member.query.dto.MemberDetailDto;


public interface MemberQueryService {
    CursorPageDto<AdminMemberQueryDto> findAll(String q, MemberStatus status, Integer warningCount, String cursor, int size);

    MemberDetailDto findMine(Long memberId);
}
