package com.b101.dib.member.command.service;

import com.b101.dib.member.command.dto.SanctionMemberRequest;
import com.b101.dib.member.domain.Member;

public interface MemberCommandService {
    Member sanction(Long memberId, SanctionMemberRequest request);
    Member releaseSanction(Long memberId);
}
