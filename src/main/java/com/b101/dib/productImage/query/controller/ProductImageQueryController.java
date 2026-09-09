package com.b101.dib.productImage.query.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.productImage.query.dto.ProductImageQueryDto;
import com.b101.dib.productImage.query.service.ProductImageQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageQueryController {
	
	private final ProductImageQueryService productImageQueryService;
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> findByProductId(
			@PathVariable("productId") Long productId
			){
		HashMap<String, Object> map = new HashMap<>();
		List<ProductImageQueryDto> dtoList = productImageQueryService.findByProductId(productId);
		map.put("message", "상품 이미지 목록 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
	
	

}
