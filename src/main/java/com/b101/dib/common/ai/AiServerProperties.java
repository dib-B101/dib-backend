package com.b101.dib.common.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// AI 서버(FastAPI) 연동 설정 dib.ai.*
// enabled=false 면 분석 요청을 보내지 않고 로그만 남긴다.
// serviceHmacSecret: 우리 → AI 서명 (AI 의 DIB_SERVICE_HMAC_SECRET), aiHmacSecret: AI → 우리 콜백 검증 (AI 의 DIB_AI_HMAC_SECRET)
@Component
@ConfigurationProperties(prefix = "dib.ai")
@Getter
@Setter
public class AiServerProperties {
    private boolean enabled = false;
    private String baseUrl = "http://localhost:8000";
    private String callbackBaseUrl = "http://localhost:8080";
    private String serviceHmacSecret = "";
    private String aiHmacSecret = "";
    private int maxSkewSeconds = 300;
}
