package com.b101.dib.order.repository;

import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.dto.OrderQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    List<OrderQueryDto> findByMemberId(@Param("memberId") Long memberId,
                                       @Param("role") OrderRole role,
                                       @Param("status") OrderStatus status);
    OrderDetailDto findById(@Param("orderId") Long orderId);
}
