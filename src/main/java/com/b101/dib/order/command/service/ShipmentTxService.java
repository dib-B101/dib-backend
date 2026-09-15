package com.b101.dib.order.command.service;

import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;

public interface ShipmentTxService {
    void markDelivered(Long orderId);
}
