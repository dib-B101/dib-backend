package com.b101.dib.product.command.dto;

import com.b101.dib.product.domain.ProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ModerateProductRequest {
    @NotNull
    private ProductStatus status;
}
