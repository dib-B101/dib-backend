package com.b101.dib.product.query.dto;

import com.b101.dib.product.domain.ProductStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminProductQueryDto {
    private Long productId;
    private Long memberId;
    private String memberNickname;
    private String title;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
