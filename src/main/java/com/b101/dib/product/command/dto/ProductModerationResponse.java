package com.b101.dib.product.command.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

// POST {AI}/internal/moderation/review 응답. AI 가 detail 에 필드를 더해도 깨지지 않게 미지정 필드는 무시한다
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductModerationResponse {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("verdict")
    private String verdict;

    // 우리 ProductStatus 이름 그대로 온다: REGISTERED | PENDING | REJECTED
    @JsonProperty("product_status")
    private String productStatus;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("category")
    private String category;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("content_hash")
    private String contentHash;

    @JsonProperty("detail")
    private Map<String, Object> detail;
}
