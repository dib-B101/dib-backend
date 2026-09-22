package com.b101.dib.common.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// 배포 프로필이 dib.ai.* 를 안 넣어서 Pod 가 localhost:8000 을 AI 서버로 찾던 사고의 재발 방지.
// 주소가 비면 조용히 틀린 곳으로 쏘는 대신 AI 를 끈다.
class AiServerConfigValidatorTest {

    private static AiServerProperties configured() {
        AiServerProperties properties = new AiServerProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("https://ai.example.com");
        properties.setCallbackBaseUrl("http://alb.example.com");
        properties.setServiceHmacSecret("service-secret");
        properties.setAiHmacSecret("ai-secret");
        return properties;
    }

    @Test
    void keepsAiEnabledWhenFullyConfigured() {
        AiServerProperties properties = configured();

        new AiServerConfigValidator(properties).validate();

        assertThat(properties.isEnabled()).isTrue();
    }

    @Test
    void disablesAiWhenBaseUrlMissing() {
        AiServerProperties properties = configured();
        properties.setBaseUrl("");

        new AiServerConfigValidator(properties).validate();

        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void disablesAiWhenCallbackUrlMissing() {
        AiServerProperties properties = configured();
        properties.setCallbackBaseUrl("");

        new AiServerConfigValidator(properties).validate();

        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void disablesAiWhenSigningSecretsMissing() {
        AiServerProperties properties = configured();
        properties.setServiceHmacSecret("");

        new AiServerConfigValidator(properties).validate();

        assertThat(properties.isEnabled()).isFalse();
    }

    @Test
    void leavesDisabledConfigAlone() {
        AiServerProperties properties = new AiServerProperties();   // enabled 기본 false

        new AiServerConfigValidator(properties).validate();

        assertThat(properties.isEnabled()).isFalse();
    }
}
