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

    // 관리자가 AI 판정 근거를 보고 승인·거절을 결정한다
    private String moderationReason;
    private String moderationStage;
    private LocalDateTime moderatedAt;
}
