package com.b101.dib.product.command.controller;

import com.b101.dib.product.command.dto.ModerateProductRequest;
import com.b101.dib.product.command.service.ProductCommandService;
import com.b101.dib.product.domain.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductCommandController {
    private final ProductCommandService productCommandService;

    @PatchMapping("/{productId}/moderation")
    public ResponseEntity<Map<String, Object>> moderate(@PathVariable("productId") Long productId,
                                                        @Valid @RequestBody ModerateProductRequest request) {
        Product product = productCommandService.moderate(productId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 검수 처리 성공");
        map.put("productId", product.getProductId());
        map.put("status", product.getStatus());
        map.put("updatedAt", product.getUpdatedAt());
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
