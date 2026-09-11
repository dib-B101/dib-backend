package com.b101.dib.product.query.controller;

import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.b101.dib.product.query.dto.ProductDetailDto;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(){
        List<ProductQueryDto> dtoList = productQueryService.findAll();
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    
    @GetMapping("/{productId}")
    public ResponseEntity<Map> findById(@PathVariable("productId") Long productId){
    	ProductDetailDto dto = productQueryService.findById(productId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 상세 조회 성공");
        map.put("data", dto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

}
