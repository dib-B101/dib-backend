package com.b101.dib.order.query.service;

import com.b101.dib.order.query.dto.ShipmentDetailDto;

public interface ShipmentQueryService {
    String findAddress(Long memberId, Long orderId);
    ShipmentDetailDto findShipment(Long memberId, Long orderId);
}
