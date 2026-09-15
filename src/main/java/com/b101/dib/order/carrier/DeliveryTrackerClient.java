package com.b101.dib.order.carrier;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.order.domain.Carrier;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "carrier.provider", havingValue = "deliverytracker")
@Slf4j
public class DeliveryTrackerClient implements CarrierClient {
    private static final String TRACK_QUERY = """
            query Track($carrierId: ID!, $trackingNumber: String!) {
              track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
                lastEvent { time status { code name } description location { name } }
                events(last: 30) { edges { node { time status { code name } description location { name } } } }
              }
            }
            """;

    private final RestClient authClient;
    private final RestClient apiClient;
    private final String basicAuth;

    private volatile String accessToken;
    private volatile long tokenExpiresAtMillis;

    public DeliveryTrackerClient(@Value("${carrier.deliverytracker.client-id}") String clientId,
                                 @Value("${carrier.deliverytracker.client-secret}") String clientSecret,
                                 @Value("${carrier.deliverytracker.auth-url:https://auth.tracker.delivery}") String authUrl,
                                 @Value("${carrier.deliverytracker.api-url:https://apis.tracker.delivery}") String apiUrl) {
        this.basicAuth = "Basic " + Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        this.authClient = RestClient.builder().baseUrl(authUrl).build();
        this.apiClient = RestClient.builder().baseUrl(apiUrl).build();
    }

    @Override
    public CarrierTracking track(String carrier, String trackingNumber) {
        String carrierId = Carrier.from(carrier).getDeliveryTrackerId();
        GraphQLResponse res = query(carrierId, trackingNumber, false);
        if (res.errors() != null && !res.errors().isEmpty()) {
            String code = res.errors().get(0).code();
            if ("UNAUTHENTICATED".equals(code)) {
                res = query(carrierId, trackingNumber, true);
            }
        }
        if (res.errors() != null && !res.errors().isEmpty()) {
            GraphQLError err = res.errors().get(0);
            String code = err.code();
            if ("NOT_FOUND".equals(code) || "BAD_REQUEST".equals(code)) {
                throw new BusinessException(ErrorCode.INVALID_TRACKING);
            }
            throw new IllegalStateException("Delivery Tracker 오류 " + code + ": " + err.message());
        }
        Track track = res.data() == null ? null : res.data().track();
        if (track == null) {
            throw new BusinessException(ErrorCode.INVALID_TRACKING);
        }
        Event last = track.lastEvent();
        String code = last == null || last.status() == null ? "UNKNOWN" : last.status().code();
        boolean delivered = "DELIVERED".equals(code);
        return new CarrierTracking(toKorean(code), delivered, toEvents(track));
    }

    private GraphQLResponse query(String carrierId, String trackingNumber, boolean refreshToken) {
        String token = token(refreshToken);
        GraphQLResponse res = apiClient.post()
                .uri("/graphql")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Map.of("query", TRACK_QUERY,
                        "variables", Map.of("carrierId", carrierId, "trackingNumber", trackingNumber)))
                .retrieve()
                .body(GraphQLResponse.class);
        if (res == null) {
            throw new IllegalStateException("Delivery Tracker 응답 없음");
        }
        return res;
    }

    private String token(boolean refresh) {
        long now = System.currentTimeMillis();
        if (!refresh && accessToken != null && now < tokenExpiresAtMillis) {
            return accessToken;
        }
        synchronized (this) {
            if (!refresh && accessToken != null && now < tokenExpiresAtMillis) {
                return accessToken;
            }
            TokenResponse res = authClient.post()
                    .uri("/oauth2/token")
                    .header(HttpHeaders.AUTHORIZATION, basicAuth)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body("grant_type=client_credentials")
                    .retrieve()
                    .body(TokenResponse.class);
            if (res == null || res.accessToken() == null) {
                throw new IllegalStateException("Delivery Tracker 토큰 발급 실패");
            }
            long ttl = res.expiresIn() == null ? 300 : Math.max(60, res.expiresIn() - 60);
            accessToken = res.accessToken();
            tokenExpiresAtMillis = now + ttl * 1000;
            return accessToken;
        }
    }

    private static List<CarrierEvent> toEvents(Track track) {
        List<CarrierEvent> out = new ArrayList<>();
        if (track.events() == null || track.events().edges() == null) {
            return out;
        }
        for (Edge edge : track.events().edges()) {
            Event e = edge.node();
            if (e == null) continue;
            String code = e.status() == null ? "UNKNOWN" : e.status().code();
            String name = e.status() == null || e.status().name() == null ? toKorean(code) : e.status().name();
            out.add(new CarrierEvent(e.time(), name, e.description(),
                    e.location() == null ? null : e.location().name()));
        }
        return out;
    }

    private static String toKorean(String code) {
        return switch (code) {
            case "INFORMATION_RECEIVED" -> "접수";
            case "AT_PICKUP" -> "집화완료";
            case "IN_TRANSIT" -> "배송중";
            case "OUT_FOR_DELIVERY" -> "배송출발";
            case "ATTEMPT_FAIL" -> "배송실패";
            case "DELIVERED" -> "배송완료";
            case "AVAILABLE_FOR_PICKUP" -> "수령대기";
            case "EXCEPTION" -> "배송이상";
            default -> "조회중";
        };
    }

    public record TokenResponse(@JsonProperty("access_token") String accessToken,
                                @JsonProperty("expires_in") Long expiresIn) {
    }

    public record GraphQLResponse(Data data, List<GraphQLError> errors) {
    }

    public record GraphQLError(String message, Map<String, Object> extensions) {
        String code() {
            return extensions == null || extensions.get("code") == null ? null : extensions.get("code").toString();
        }
    }

    public record Data(Track track) {
    }

    public record Track(Event lastEvent, Events events) {
    }

    public record Events(List<Edge> edges) {
    }

    public record Edge(Event node) {
    }

    public record Event(String time, Status status, String description, Location location) {
    }

    public record Status(String code, String name) {
    }

    public record Location(String name) {
    }
}
