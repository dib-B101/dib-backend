package com.b101.dib.order.query.dto;

import com.b101.dib.order.carrier.CarrierEvent;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.b101.dib.order.domain.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ShipmentDetailDto {
    private Long orderId;
    private String carrier;
    private String carrierName;
    private String trackingNumber;
    private OrderStatus status;
    private String carrierStatus;
    private List<CarrierEvent> events;
    private LocalDateTime lastCheckedAt;
    @JsonProperty("isStale")
    private boolean stale;
}
