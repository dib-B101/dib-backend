package com.b101.dib.common.ai;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// dib.ai.enabled 만 켜고 주소·서명키를 안 채운 채 뜨는 사고를 기동 시점에 잡는다.
// 예전에 배포 프로필이 dib.ai.* 를 안 넣어서 Pod 가 자기 자신(localhost:8000)을 AI 서버로 찾았고,
// 요청은 전부 실패하는데 화면에는 "추천이 비어 있다" 정도로만 보여서 한참 못 찾았다.
//
// 기동을 막지는 않는다 — AI 는 부가 기능이고, 이것 때문에 서비스 전체가 안 뜨면 더 나쁘다.
// 대신 AI 를 끄고 이유를 로그에 남긴다. 조용히 틀린 주소로 계속 쏘는 것보다 낫다.
@Component
@RequiredArgsConstructor
@Slf4j
public class AiServerConfigValidator {

    private final AiServerProperties properties;

    @PostConstruct
    public void validate() {
        if (!properties.isEnabled()) {
            log.info("AI 연동 비활성 (dib.ai.enabled=false)");
            return;
        }

        List<String> missing = new ArrayList<>();
        if (isBlank(properties.getBaseUrl())) missing.add("dib.ai.base-url (DIB_AI_BASE_URL)");
        if (isBlank(properties.getCallbackBaseUrl())) missing.add("dib.ai.callback-base-url (DIB_AI_CALLBACK_BASE_URL)");
        if (isBlank(properties.getServiceHmacSecret())) missing.add("dib.ai.service-hmac-secret (DIB_SERVICE_HMAC_SECRET)");
        if (isBlank(properties.getAiHmacSecret())) missing.add("dib.ai.ai-hmac-secret (DIB_AI_HMAC_SECRET)");

        if (!missing.isEmpty()) {
            properties.setEnabled(false);
            log.error("dib.ai.enabled=true 인데 설정이 비어 있어 AI 연동을 끕니다. 채워야 할 값: {}", missing);
            return;
        }

        log.info("AI 연동 활성 base-url={} callback-base-url={}",
                properties.getBaseUrl(), properties.getCallbackBaseUrl());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
