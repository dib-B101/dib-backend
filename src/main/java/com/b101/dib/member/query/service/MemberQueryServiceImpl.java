package com.b101.dib.member.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;
import com.b101.dib.member.query.dto.MemberDetailDto;
import com.b101.dib.member.repository.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryServiceImpl implements MemberQueryService {
    private final MemberMapper memberMapper;

    @Override
    public CursorPageDto<AdminMemberQueryDto> findAll(String q, MemberStatus status, Integer warningCount, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<AdminMemberQueryDto> rows = memberMapper.findAll(q, status, warningCount, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, AdminMemberQueryDto::getMemberId);
    }

    @Override
    public MemberDetailDto findMine(Long memberId) {
        MemberDetailDto member = memberMapper.findById(memberId);
        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }
}
