package com.b101.dib.order.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.query.dto.OrderDetailViewDto;
import com.b101.dib.order.query.dto.OrderQueryDto;
import com.b101.dib.order.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {
    private final OrderMapper orderMapper;

    @Override
    public CursorPageDto<OrderQueryDto> findMine(Long memberId, OrderRole role, OrderStatus status, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<OrderQueryDto> rows = orderMapper.findByMemberId(memberId, role, status, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, OrderQueryDto::getOrderId);
    }

    @Override
    public OrderDetailViewDto findDetail(Long memberId, Long orderId) {
        OrderDetailDto row = orderMapper.findById(orderId);
        if (row == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!row.getBuyerId().equals(memberId) && !row.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return OrderDetailViewDto.from(row);
    }
}
