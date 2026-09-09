package com.b101.dib.product.command.controller;

import com.b101.dib.product.command.dto.CreateRequest;
import com.b101.dib.product.command.dto.UpdateRequest;
import com.b101.dib.product.command.service.ProductCommandService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Member;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductCommandController {
    private final ProductCommandService productCommandService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            @Valid @RequestPart("request") CreateRequest createRequest,

            @RequestPart(value = "images", required = false)
            List<MultipartFile> images
    ) {
    	Long myId = 1L;
        Long productId = productCommandService.create(myId, createRequest, images);

        Map<String, Object> map = new HashMap<>();
        map.put("message", "상품 등록 성공");
        map.put("productId", productId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(map);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> update(
          @PathVariable("productId") Long productId,
          @RequestBody UpdateRequest request){
    	Long myId = 1L;
        productCommandService.update(myId, productId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 정보 수정 성공");
        map.put("productId", productId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(map);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> delete(
    		@PathVariable("productId") Long productId){
    	Long myId = 1L;
        productCommandService.delete(myId, productId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 삭제 성공");
        return ResponseEntity
        		.status(HttpStatus.NO_CONTENT)
        		.body(map);
    }
}
