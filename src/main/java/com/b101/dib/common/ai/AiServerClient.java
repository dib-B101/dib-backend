package com.b101.dib.common.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

// FastAPI 내부 API 호출 (명세 92 · 94). 본문을 한 번 직렬화해 그 바이트에 서명하고 그대로 보낸다
@Component
@Slf4j
public class AiServerClient {
    public static final String BID_ANOMALIES_PATH = "/internal/v1/ai/bid-anomalies";
    public static final String RECOMMENDATIONS_PATH = "/internal/v1/ai/recommendations";
    // moderation_api 는 hmac_auth 를 쓰지 않는다(serve.py 가 라우터만 붙이고 검증은 internal_api 핸들러 안에만 있다). 서명 없이 부른다
    public static final String MODERATION_REVIEW_PATH = "/internal/moderation/review";

    private final AiServerProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public AiServerClient(AiServerProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        // Java 25 기본 HTTP 클라이언트의 h2c 업그레이드는 Uvicorn과 조합할 때
        // 요청 본문이 빈 값으로 먼저 처리될 수 있다. 내부 AI 호출은 명시적으로
        // HTTP/1.1 기반 HttpURLConnection을 사용한다.
        this.restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .baseUrl(properties.getBaseUrl())
                .build();
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
                    // StringHttpMessageConverter가 Content-Type charset을 보정하며 본문을
                    // 다시 인코딩하면 서명한 바이트와 실제 전송 바이트가 달라질 수 있다.
                    // 서명한 byte[] 자체를 보내 HMAC 대상과 wire body를 반드시 같게 둔다.
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("AI 요청 실패 {} {} — {}", label, path, e.getMessage());
            return false;
        }
    }

    // 동기 호출 + 응답 본문 파싱. 실패하면 null 을 주고 호출자가 기존 동작을 유지한다 (예외를 밖으로 흘리지 않는다)
    public <T> T postForObject(String path, Object request, Class<T> responseType, String label) {
        try {
            return restClient.post().uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsBytes(request))
                    .retrieve()
                    .body(responseType);
        } catch (Exception e) {
            log.warn("AI 동기 요청 실패 {} {} — {}", label, path, e.getMessage());
            return null;
        }
    }
}
