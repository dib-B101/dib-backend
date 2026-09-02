package com.b101.dib.product.query.controller;

import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public ResponseEntity<Map> findAll(){
        List<ProductQueryDto> dtoList = productQueryService.findAll();
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 전체 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(map);
    }

}
