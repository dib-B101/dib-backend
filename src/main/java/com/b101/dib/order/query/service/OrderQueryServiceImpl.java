package com.b101.dib.order.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.OrderRole;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderDetailDto;
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
    public List<OrderQueryDto> findMine(Long memberId, OrderRole role, OrderStatus status) {
        return orderMapper.findByMemberId(memberId, role, status);
    }

    @Override
    public OrderDetailDto findDetail(Long memberId, Long orderId) {
        OrderDetailDto dto = orderMapper.findById(orderId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        if (!dto.getBuyerId().equals(memberId) && !dto.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return dto;
    }
}
