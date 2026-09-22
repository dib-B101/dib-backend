package com.b101.dib.category.query.controller;

import com.b101.dib.category.query.service.CategoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// 프론트 ProductRemoteDataSource.getCategories → data: {items: [{categoryId, name}]}
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryQueryController {
    private final CategoryQueryService categoryQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        Map<String, Object> data = new HashMap<>();
        data.put("items", categoryQueryService.findAll());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "카테고리 목록 조회 성공");
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
