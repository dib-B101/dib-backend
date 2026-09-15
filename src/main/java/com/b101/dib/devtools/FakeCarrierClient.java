package com.b101.dib.devtools;

import com.b101.dib.order.carrier.CarrierClient;
import com.b101.dib.order.carrier.CarrierTracking;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "carrier.provider", havingValue = "fake", matchIfMissing = true)
public class FakeCarrierClient implements CarrierClient {
    @Override
    public CarrierTracking track(String carrier, String trackingNumber) {
        boolean delivered = trackingNumber != null && trackingNumber.endsWith("9");
        return new CarrierTracking(delivered ? "배송완료" : "배송중", delivered);
    }
}
