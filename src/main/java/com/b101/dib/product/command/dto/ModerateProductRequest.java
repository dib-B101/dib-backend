package com.b101.dib.product.command.dto;

import com.b101.dib.product.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModerateProductRequest {
    @NotNull
    private ProductStatus status;

    // 거절 사유는 판매자에게 그대로 보인다. 비우면 AI 가 남긴 사유를 유지한다
    @Size(max = 1000)
    private String reason;
}
