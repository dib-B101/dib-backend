package com.b101.dib.order.carrier;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Carrier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@ConditionalOnProperty(name = "carrier.provider", havingValue = "sweettracker")
public class SweetTrackerClient implements CarrierClient {
    private final RestClient restClient;
    private final String apiKey;

    public SweetTrackerClient(@Value("${carrier.sweettracker.key}") String apiKey,
                              @Value("${carrier.sweettracker.base-url:https://info.sweettracker.co.kr}") String baseUrl) {
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public CarrierTracking track(String carrier, String trackingNumber) {
        String code = Carrier.from(carrier).getSweetTrackerCode();
        if (code == null) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_CARRIER);
        }
        Response res = restClient.get()
                .uri("/api/v1/trackingInfo?t_key={key}&t_code={code}&t_invoice={invoice}", apiKey, code, trackingNumber)
                .retrieve()
                .body(Response.class);
        if (res == null || Boolean.FALSE.equals(res.status())) {
            throw new BusinessException(ErrorCode.INVALID_TRACKING);
        }
        boolean delivered = Boolean.TRUE.equals(res.complete()) || (res.level() != null && res.level() >= 6);
        return new CarrierTracking(levelText(res.level(), delivered), delivered);
    }

    private static String levelText(Integer level, boolean delivered) {
        if (delivered) return "배송완료";
        if (level == null) return "조회중";
        return switch (level) {
            case 1 -> "배송준비중";
            case 2 -> "집화완료";
            case 3 -> "배송중";
            case 4 -> "지점도착";
            case 5 -> "배송출발";
            default -> "배송중";
        };
    }

    public record Response(Boolean status, Boolean complete, Integer level, String msg) {
    }
}
