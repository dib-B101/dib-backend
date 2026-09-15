package com.b101.dib.order.carrier;

public interface CarrierClient {
    CarrierTracking track(String carrier, String trackingNumber);
}
