package com.b101.dib.order.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailViewDto;
import com.b101.dib.order.query.dto.OrderQueryDto;


public interface OrderQueryService {
    CursorPageDto<OrderQueryDto> findMine(Long memberId, OrderRole role, OrderStatus status, String cursor, int size);
    OrderDetailViewDto findDetail(Long memberId, Long orderId);
}
