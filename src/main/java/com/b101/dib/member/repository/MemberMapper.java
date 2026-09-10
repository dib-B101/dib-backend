package com.b101.dib.member.repository;

import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<AdminMemberQueryDto> findAll(@Param("q") String q,
                                      @Param("status") MemberStatus status,
                                      @Param("warningCount") Integer warningCount);
}
