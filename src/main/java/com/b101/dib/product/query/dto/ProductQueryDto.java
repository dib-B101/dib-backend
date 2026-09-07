package com.b101.dib.product.query.dto;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor      // MyBatis 가 빈 객체를 만들고 setter 로 채운다
public class ProductQueryDto {
    private Long productId;
    private Long memberId;
    private Long categoryId;
    private String title;
    private String description;
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;
    private ProductStatus status;
//    private String embedding;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
