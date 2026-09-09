package com.b101.dib.member.query.service;

import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;
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
    public List<AdminMemberQueryDto> findAll(String q, MemberStatus status, Integer warningCount) {
        return memberMapper.findAll(q, status, warningCount);
    }
}
