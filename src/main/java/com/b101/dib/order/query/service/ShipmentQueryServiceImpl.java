package com.b101.dib.order.query.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.carrier.CarrierClient;
import com.b101.dib.order.carrier.CarrierEvent;
import com.b101.dib.order.carrier.CarrierTracking;
import com.b101.dib.order.carrier.CarrierTrackingCache;
import com.b101.dib.order.command.service.ShipmentTxService;
import com.b101.dib.order.domain.Carrier;
import com.b101.dib.order.domain.OrderStatus;
import com.b101.dib.order.query.dto.OrderShipmentDto;
import com.b101.dib.order.query.dto.ShipmentDetailDto;
import com.b101.dib.order.repository.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentQueryServiceImpl implements ShipmentQueryService {   // 외부 배송 API를 부르므로 @Transactional 없음
    private final OrderMapper orderMapper;
    private final CarrierClient carrierClient;
    private final CarrierTrackingCache trackingCache;
    private final ShipmentTxService shipmentTxService;

    @Override
    public String findAddress(Long memberId, Long orderId) {
        OrderShipmentDto dto = load(orderId);
        if (dto.getBuyerId().equals(memberId)) {
            return dto.getAddress();
        }
        if (!dto.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        OrderStatus s = dto.getStatus();
        if (s == OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_REQUIRED);
        }
        if (s == OrderStatus.CONFIRMED || s == OrderStatus.CANCELED || s == OrderStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return dto.getAddress();
    }

    @Override
    public ShipmentDetailDto findShipment(Long memberId, Long orderId) {
        OrderShipmentDto dto = load(orderId);
        if (!dto.getBuyerId().equals(memberId) && !dto.getSellerId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (dto.getTrackingNumber() == null) {
            throw new BusinessException(ErrorCode.SHIPMENT_NOT_FOUND);
        }
        OrderStatus status = dto.getStatus();
        String carrierStatus = null;
        List<CarrierEvent> events = List.of();
        boolean stale = false;
        if (status == OrderStatus.SHIPPED) {
            try {
                CarrierTracking tracking = trackingCache.get(dto.getCarrier(), dto.getTrackingNumber()).orElse(null);
                if (tracking == null) {
                    tracking = carrierClient.track(dto.getCarrier(), dto.getTrackingNumber());
                    trackingCache.put(dto.getCarrier(), dto.getTrackingNumber(), tracking);
                }
                carrierStatus = tracking.status();
                events = tracking.events();
                if (tracking.delivered()) {
                    shipmentTxService.markDelivered(orderId);
                    status = OrderStatus.DELIEVERED;
                }
            } catch (BusinessException e) {
                if (e.getErrorCode() != ErrorCode.INVALID_TRACKING) {
                    throw e;
                }
                carrierStatus = "INVALID_TRACKING";   // 택배사에 없는 송장 — 판매자가 잘못 입력했거나 아직 집화 스캔 전
            } catch (Exception e) {
                log.warn("배송 조회 실패 orderId={} carrier={} tracking={}", orderId, dto.getCarrier(), dto.getTrackingNumber(), e);
                stale = true;
            }
        }
        ShipmentDetailDto result = new ShipmentDetailDto();
        result.setOrderId(orderId);
        result.setCarrier(dto.getCarrier());
        result.setCarrierName(carrierName(dto.getCarrier()));
        result.setTrackingNumber(dto.getTrackingNumber());
        result.setStatus(status);
        result.setCarrierStatus(carrierStatus);
        result.setEvents(events);
        result.setLastCheckedAt(LocalDateTime.now());
        result.setStale(stale);
        return result;
    }

    private static String carrierName(String code) {
        if (code == null) {
            return null;
        }
        try {
            return Carrier.valueOf(code).getDisplayName();
        } catch (IllegalArgumentException e) {
            return code;
        }
    }

    private OrderShipmentDto load(Long orderId) {
        OrderShipmentDto dto = orderMapper.findShipment(orderId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }
        return dto;
    }
}
