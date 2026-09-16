package com.b101.dib.category.query.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.category.query.dto.CategoryQueryDto;
import com.b101.dib.category.query.service.CategoryQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryQueryController {
	
	private final CategoryQueryService categoryQueryService;
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> findAll(){
		List<CategoryQueryDto> dtoList = categoryQueryService.findAll();
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "카테고리 전체 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

}
