package com.b101.dib.order.query.service;

import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.dto.OrderQueryDto;

import java.util.List;

public interface OrderQueryService {
    List<OrderQueryDto> findMine(Long memberId, OrderRole role, OrderStatus status);
    OrderDetailDto findDetail(Long memberId, Long orderId);
}
