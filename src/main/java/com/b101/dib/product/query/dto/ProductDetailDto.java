package com.b101.dib.product.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter                
@NoArgsConstructor
public class ProductDetailDto {
    private Long productId;
    private String title;
    private String description;
    private ProductCondition condition;
    private String modelName;
    private Integer releaseYear;
    private Long marketPrice;
    private String thumbnailUrl;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CategorySummary category;
    private SellerSummary seller;
    private List<ImageItem> images;

    @Getter @Setter @NoArgsConstructor
    public static class CategorySummary {
        private Long categoryId;
        private String name;
    }

    @Getter @Setter @NoArgsConstructor
    public static class SellerSummary {
        private Long memberId;
        private String nickname;
        private Double score;
    }

    @Getter @Setter @NoArgsConstructor
    public static class ImageItem {
        private String imageUrl;
        private String type;
    }
}