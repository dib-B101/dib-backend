package com.b101.dib.product.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.domain.ProductStatus;

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
    
    private Long categoryId;
    private String categoryName;
    
    private Long memberId;
    private String nickname;

}