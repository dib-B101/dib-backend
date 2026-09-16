package com.b101.dib.category.query.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryQueryDto {
	private Long categoryId;
	private String name;
}
