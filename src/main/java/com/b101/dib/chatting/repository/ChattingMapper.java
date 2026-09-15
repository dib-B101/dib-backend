package com.b101.dib.chatting.repository;

import com.b101.dib.chatting.query.dto.ChattingQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChattingMapper {
    List<ChattingQueryDto> findByOrderId(@Param("orderId") Long orderId,
                                         @Param("beforeChattingId") Long beforeChattingId,
                                         @Param("limit") int limit);
}
