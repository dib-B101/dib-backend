package com.b101.dib.product.command.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// POST {AI}/internal/moderation/review 요청. AI 계약이 스네이크케이스라 필드명을 명시한다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductModerationRequest {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("description")
    private String description;

    @JsonProperty("image_urls")
    private List<String> imageUrls;

    @JsonProperty("category_name")
    private String categoryName;
}
