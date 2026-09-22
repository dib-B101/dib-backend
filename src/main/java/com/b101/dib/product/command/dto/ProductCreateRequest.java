package com.b101.dib.product.command.dto;

import com.b101.dib.product.domain.ProductCondition;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

	@NotNull
	@Positive
	private Long categoryId;
    @NotBlank
    @Size(max = 200)
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private ProductCondition condition;
    @Size(max = 100)
    private String modelName;
    @Min(1900)
    @Max(2100)
    private Integer releaseYear;
    @PositiveOrZero
    private Long marketPrice;

    // 시작가·경매시간은 경매 시작 또는 라이브 편성 시점에 정한다. 등록 때는 선택
    @Min(1000)
    private Long startPrice;
    // 등록 시점의 경매는 아직 라이브 편성 전이라 일반 경매 하한(5분)을 적용한다.
    // 라이브에 올릴 물건이면 여기서는 비워 두고 편성(PUT /live-broadcasts/{id}/items)에서 30초~5분으로 정한다
    @Min(300)
    private Integer auctionTime;
}
