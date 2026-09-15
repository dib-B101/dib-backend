package com.b101.dib.member.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;


public interface MemberQueryService {
    CursorPageDto<AdminMemberQueryDto> findAll(String q, MemberStatus status, Integer warningCount, String cursor, int size);
}
