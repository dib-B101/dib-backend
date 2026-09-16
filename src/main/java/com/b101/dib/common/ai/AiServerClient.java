package com.b101.dib.common.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

// FastAPI 내부 API 호출 (명세 92 · 94). 본문을 한 번 직렬화해 그 바이트에 서명하고 그대로 보낸다
@Component
@Slf4j
public class AiServerClient {
    public static final String BID_ANOMALIES_PATH = "/internal/v1/ai/bid-anomalies";
    public static final String RECOMMENDATIONS_PATH = "/internal/v1/ai/recommendations";

    private final AiServerProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiServerClient(AiServerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public String callbackUrl(String path) {
        return properties.getCallbackBaseUrl() + path;
    }

    // 202 면 true. 400(INVALID_PAYLOAD)·503(MODEL_UNAVAILABLE)·연결 실패는 false + 로그 (분석 실패가 종료 처리를 막지 않는다)
    public boolean postAccepted(String path, Object request, String label) {
        byte[] body = objectMapper.writeValueAsBytes(request);
        long ts = HmacSigner.nowSeconds();
        try {
            restClient.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HmacSigner.TIMESTAMP_HEADER, String.valueOf(ts))
                    .header(HmacSigner.SIGNATURE_HEADER, HmacSigner.sign(properties.getServiceHmacSecret(), ts, body))
                    .body(new String(body, StandardCharsets.UTF_8))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("AI 요청 실패 {} {} — {}", label, path, e.getMessage());
            return false;
        }
    }
}
