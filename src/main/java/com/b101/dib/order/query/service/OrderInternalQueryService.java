package com.b101.dib.order.query.service;

import com.b101.dib.order.query.dto.OrderDetailDto;

public interface OrderInternalQueryService {
    OrderDetailDto find(Long orderId);
}
