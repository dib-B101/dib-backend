package com.b101.dib.product.query.controller;

import com.b101.dib.product.domain.ProductStatus;
import com.b101.dib.product.query.dto.AdminProductQueryDto;
import com.b101.dib.product.query.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductQueryController {
    private final ProductQueryService productQueryService;

    @GetMapping("/moderation")
    public ResponseEntity<Map<String, Object>> findForModeration(@RequestParam(name = "status", required = false) ProductStatus status) {
        List<AdminProductQueryDto> dtoList = productQueryService.findForModeration(status);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 검수 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
