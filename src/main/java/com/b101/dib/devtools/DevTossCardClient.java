package com.b101.dib.devtools;

import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.payment.toss.TossApiException;
import com.b101.dib.payment.toss.TossBillingKeyResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * 로컬 테스트 전용. Toss 카드번호 직접 빌링키 발급(/v1/billing/authorizations/card).
 * 실서비스는 카드번호를 서버가 만지지 않는다 — 프론트 SDK의 authKey 경로(PaymentMethodCommandController)만 쓴다.
 */
@Component
@Profile("local")
public class DevTossCardClient {
    private final RestClient restClient;

    public DevTossCardClient(@Value("${toss.secret-key}") String secretKey,
                             @Value("${toss.base-url}") String baseUrl) {
        String basic = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public TossBillingKeyResponse issueByCard(String customerKey, String cardNumber, String expirationYear,
                                              String expirationMonth, String customerIdentityNumber) {
        try {
            return restClient.post()
                    .uri("/v1/billing/authorizations/card")
                    .body(Map.of("customerKey", customerKey, "cardNumber", cardNumber,
                            "cardExpirationYear", expirationYear, "cardExpirationMonth", expirationMonth,
                            "customerIdentityNumber", customerIdentityNumber))
                    .retrieve()
                    .body(TossBillingKeyResponse.class);
        } catch (RestClientResponseException e) {
            throw new TossApiException(ErrorCode.BILLING_KEY_ISSUE_FAILED, e.getResponseBodyAsString());
        }
    }
}
