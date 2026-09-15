package com.b101.dib.address.repository;

import java.util.List;

import com.b101.dib.address.query.dto.AddressQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AddressMapper {

    List<AddressQueryDto> findByMemberId(@Param("memberId") Long memberId);
}
