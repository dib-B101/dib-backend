package com.b101.dib.product.command.controller;

import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.command.dto.ProductUpdateRequest;
import com.b101.dib.product.command.service.ProductCommandService;
import com.b101.dib.product.domain.Product;

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
//            @RequestHeader("X-Member-Id") Long myId,
            @Valid @RequestPart("request") ProductCreateRequest createRequest,
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images
    ) {
    	Long myId = 1L;
        Product product = productCommandService.create(myId, createRequest, images);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "상품 등록 성공");
        map.put("data", product);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(map);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> update(
//          @RequestHeader("X-Member-Id") Long myId,
          @PathVariable("productId") Long productId,
          @RequestBody ProductUpdateRequest request){
    	Long myId = 1L;
        Product product = productCommandService.update(myId, productId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 정보 수정 성공");
        map.put("data", product);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(map);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> delete(
//    		@RequestHeader("X-Member-Id") Long myId,
    		@PathVariable("productId") Long productId){
    	Long myId = 1L;
        Product product = productCommandService.delete(myId, productId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 삭제 성공");
        map.put("data", product);
        return ResponseEntity
        		.status(HttpStatus.NO_CONTENT)
        		.body(map);
    }
    
    @PatchMapping("/{productId}/auctions/start")
    public ResponseEntity<Map<String, Object>> auctionStart(
    		@RequestHeader("X-Member-Id") Long myId,
    		@PathVariable("productId") Long productId){
        Product product = productCommandService.startAuction(myId, productId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 경매 시작");
        map.put("data", product);
        return ResponseEntity
        		.status(HttpStatus.NO_CONTENT)
        		.body(map);
    }
}
