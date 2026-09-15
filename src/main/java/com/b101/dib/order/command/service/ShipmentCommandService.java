package com.b101.dib.order.command.service;

import com.b101.dib.order.command.dto.ShipmentRequest;
import com.b101.dib.order.command.dto.UpdateAddressRequest;
import com.b101.dib.order.domain.Order;

public interface ShipmentCommandService {
    Order updateAddress(Long memberId, Long orderId, UpdateAddressRequest request);
    Order ship(Long memberId, Long orderId, ShipmentRequest request);
}
