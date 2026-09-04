package com.b101.dib.product.command.dto;

import com.b101.dib.product.query.dto.ProductCondition;
import com.b101.dib.product.query.dto.ProductStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRequest {
	@NotNull
    private Long categoryId;

	@NotBlank(message = "제목은 필수입니다")
    @Size(max = 200)
    private String title;

    private String description;

    @NotNull
    private ProductCondition condition;

    @Size(max = 200)
    private String modelName;
    private Integer releaseYear;

    @PositiveOrZero
    private Long marketPrice;

    @Size(max = 500)
    private String thumbnailUrl;
}
