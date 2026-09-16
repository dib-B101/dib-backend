package com.b101.dib.member.command.service;

import com.b101.dib.member.command.dto.SanctionMemberRequest;
import com.b101.dib.member.command.dto.UpdateProfileRequest;
import com.b101.dib.member.command.dto.UpdateProfileResponse;
import com.b101.dib.member.domain.Member;

public interface MemberCommandService {

    /** 회원을 제재한다. */
    Member sanction(Long memberId, SanctionMemberRequest request);

    /** 회원 제재를 해제한다. */
    Member releaseSanction(Long memberId);

    /** 회원 프로필을 수정한다. */
    UpdateProfileResponse updateProfile(Long memberId, UpdateProfileRequest request);
}
