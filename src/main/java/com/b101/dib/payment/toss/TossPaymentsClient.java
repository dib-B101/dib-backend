package com.b101.dib.payment.toss;

import com.b101.dib.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
public class TossPaymentsClient {
    private final RestClient restClient;

    public TossPaymentsClient(@Value("${toss.secret-key}") String secretKey,
                              @Value("${toss.base-url}") String baseUrl) {
        String basic = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TossBillingKeyResponse issueBillingKey(String authKey, String customerKey) {
        try {
            return restClient.post()
                    .uri("/v1/billing/authorizations/issue")
                    .body(Map.of("authKey", authKey, "customerKey", customerKey))
                    .retrieve()
                    .body(TossBillingKeyResponse.class);
        } catch (RestClientResponseException e) {
            throw new TossApiException(ErrorCode.BILLING_KEY_ISSUE_FAILED, e.getResponseBodyAsString());
        }
    }

    public TossPaymentResponse chargeBilling(String billingKey, String customerKey, long amount,
                                             String tossOrderId, String orderName) {
        try {
            return restClient.post()
                    .uri("/v1/billing/{billingKey}", billingKey)
                    .body(Map.of("customerKey", customerKey, "amount", amount,
                            "orderId", tossOrderId, "orderName", orderName))
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (RestClientResponseException e) {
            throw new TossApiException(ErrorCode.TOSS_CONFIRM_FAILED, e.getResponseBodyAsString());
        }
    }
}
