package com.b101.dib.order.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.query.dto.OrderDetailDto;
import com.b101.dib.order.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderInternalQueryServiceImpl implements OrderInternalQueryService {
    private final OrderMapper orderMapper;

    @Override
    public OrderDetailDto find(Long orderId) {
        OrderDetailDto dto = orderMapper.findById(orderId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return dto;
    }
}
