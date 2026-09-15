package com.b101.dib.order.command.service;

import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentTxServiceImpl implements ShipmentTxService {
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public void markDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || order.getStatus() != OrderStatus.SHIPPED) {
            return;
        }
        order.deliver();
        notificationRepository.save(Notification.system(order.getBuyerId(), "배송 완료",
                "주문 #" + orderId + " 상품이 도착했습니다. 확인 후 구매 확정을 눌러 주세요."));
    }
}
