package com.b101.dib.product.command.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.product.command.dto.ProductCreateRequest;
import com.b101.dib.product.command.dto.ProductUpdateRequest;
import com.b101.dib.product.command.service.ProductCommandService;
import com.b101.dib.product.domain.Product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @AuthenticationPrincipal AccessTokenClaims claims,
            @Valid @RequestPart("request") ProductCreateRequest createRequest,
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images
    ) {
        Product product = productCommandService.create(claims.memberId(), createRequest, images);
        Map<String, Object> map = new HashMap<>();
        map.put("message", "상품 등록 성공");
        map.put("data", product);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(map);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> update(
          @AuthenticationPrincipal AccessTokenClaims claims,
          @PathVariable("productId") Long productId,
          @RequestBody ProductUpdateRequest request){
        Product product = productCommandService.update(claims.memberId(), productId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 정보 수정 성공");
        map.put("data", product);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(map);
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> delete(
            @AuthenticationPrincipal AccessTokenClaims claims,
    		@PathVariable("productId") Long productId){
        Product product = productCommandService.delete(claims.memberId(), productId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 삭제 성공");
        map.put("data", product);
        return ResponseEntity
        		.status(HttpStatus.NO_CONTENT)
        		.body(map);
    }
    
    @PatchMapping("/{productId}/auctions/start")
    public ResponseEntity<Map<String, Object>> auctionStart(
			@AuthenticationPrincipal AccessTokenClaims claims,
    		@PathVariable("productId") Long productId){
        Product product = productCommandService.startAuction(claims.memberId(), productId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 경매 시작");
        map.put("data", product);
        return ResponseEntity
        		.status(HttpStatus.NO_CONTENT)
        		.body(map);
    }
}
