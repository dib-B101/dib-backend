package com.b101.dib.product.command.controller;

import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.command.service.ProductCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductCommandController {
    private final ProductCommandService productCommandService;

    @PostMapping
    public ResponseEntity<Map> create(@RequestBody CreateRequest createRequest){
        Long productId = productCommandService.create(createRequest);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 등록 성공");
        map.put("productId", productId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(map);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Map> update(@PathVariable("productId") Long productId,
                                      @RequestBody UpdateRequest request){
        productCommandService.update(productId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 정보 수정 성공");
        map.put("productId", productId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(map);
    }
}
