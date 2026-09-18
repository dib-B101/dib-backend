package com.b101.dib.order.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
import com.b101.dib.order.command.dto.ShipmentRequest;
import com.b101.dib.order.command.dto.UpdateAddressRequest;
import com.b101.dib.order.domain.Carrier;
import com.b101.dib.order.domain.Order;
import com.b101.dib.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentCommandServiceImpl implements ShipmentCommandService {
    private static final Pattern TRACKING = Pattern.compile("^[0-9]{9,14}$");
    // Delivery Tracker 개발용 가상 택배사 송장 형식 (yyyy-MM-ddTHH:00:00Z)
    private static final Pattern DUMMY_TRACKING = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:00:00Z$");

    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public Order updateAddress(Long memberId, Long orderId, UpdateAddressRequest request) {
        Order order = find(orderId);
        if (!order.isBuyer(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        order.updateAddress(request.toJson());
        return order;
    }

    @Override
    public Order ship(Long memberId, Long orderId, ShipmentRequest request) {
        Order order = find(orderId);
        if (!order.isSeller(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        Carrier carrier = Carrier.from(request.getCarrier());
        String rawTracking = request.getTrackingNumber().trim();
        String tracking = carrier.isTestOnly() ? rawTracking : rawTracking.replace("-", "");
        Pattern format = carrier.isTestOnly() ? DUMMY_TRACKING : TRACKING;
        if (!format.matcher(tracking).matches()) {
            throw new BusinessException(ErrorCode.INVALID_TRACKING);
        }
        order.ship(carrier.name(), tracking);
        notificationRepository.save(Notification.system(order.getBuyerId(), "배송 시작",
                "주문 #" + orderId + " 상품이 발송되었습니다. " + carrier.getDisplayName() + " " + tracking));
        return order;
    }

    private Order find(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }
}
