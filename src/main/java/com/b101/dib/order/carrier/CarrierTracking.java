package com.b101.dib.order.carrier;

import java.util.List;

public record CarrierTracking(String status, boolean delivered, List<CarrierEvent> events) {

    public CarrierTracking(String status, boolean delivered) {
        this(status, delivered, List.of());
    }
}
