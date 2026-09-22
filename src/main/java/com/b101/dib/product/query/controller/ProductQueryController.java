package com.b101.dib.product.query.controller;

import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.b101.dib.product.domain.ProductCondition;
import com.b101.dib.product.query.dto.ProductDetailDto;
import com.b101.dib.product.query.dto.ProductListDto;
import com.b101.dib.product.query.dto.ProductSearchFilter;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductQueryController {

    private final ProductQueryService productQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ){
        CursorPageDto<ProductQueryDto> page = productQueryService.findAll(cursor, size);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 목록 조회 성공");
        map.put("data", page);
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
    
    @GetMapping("/members/me")
    public ResponseEntity<Map<String, Object>> findMyProducts(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
    ){
		CursorPageDto<ProductListDto> page = productQueryService.findMyProducts(claims.memberId(), cursor, size);
    	HashMap<String, Object> map = new HashMap<>();
        map.put("message", "내 상품 목록 조회 성공");
        map.put("data", page);
        return ResponseEntity
        		.status(HttpStatus.OK)
        		.body(map);
    	
    }
    
    @GetMapping("/members/{memberId}")
    public ResponseEntity<Map<String, Object>> findByMemberId(
    		@PathVariable("memberId") Long memberId
    		){
    	List<ProductListDto> dtoList = productQueryService.findByMemberId(memberId);
    	HashMap<String, Object> map = new HashMap<>();
        map.put("message", "회원 상품 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity
        		.status(HttpStatus.OK)
        		.body(map);
    	
    }
    
    // keyword 는 이제 선택이다. 필터만 걸고 둘러보는 것도 검색이다
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "minPrice", required = false) Long minPrice,
            @RequestParam(name = "maxPrice", required = false) Long maxPrice,
            @RequestParam(name = "condition", required = false) ProductCondition condition,
            @RequestParam(name = "onAuctionOnly", required = false) Boolean onAuctionOnly,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size
            ){
        ProductSearchFilter filter = ProductSearchFilter.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .condition(condition)
                .onAuctionOnly(onAuctionOnly)
                .build();
        CursorPageDto<ProductListDto> page = productQueryService.search(filter, cursor, size);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "상품 목록 검색 성공");
        map.put("data", page);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

}
