package com.b101.dib.product.query.service;

import com.b101.dib.product.query.dto.CursorPage;
import com.b101.dib.product.query.dto.ProductQueryDto;
import com.b101.dib.product.query.dto.ProductSearchCondition;
import com.b101.dib.product.query.mapper.ProductQueryMapper;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductQueryServiceImpl implements ProductQueryService {
    private final ProductQueryMapper productQueryMapper;

    @Override
    public CursorPage<ProductQueryDto> findAll(ProductSearchCondition cond, String cursor) {
        decodeCursor(cond, cursor);                                   // ① 커서 문자열 → cond 에 풀어 넣기

        List<ProductQueryDto> rows = productQueryMapper.findAll(cond); // ② size+1 개 조회

        int size = Math.min(cond.getSize(), 50);
        boolean hasNext = rows.size() > size;                          // ③ 1개 더 왔으면 다음 페이지 있음
        List<ProductQueryDto> items = hasNext ? rows.subList(0, size) : rows;

        String nextCursor = null;
        if (hasNext) {                                                 // ④ 마지막 항목으로 다음 커서 만들기
            ProductQueryDto last = items.get(items.size() - 1);
            nextCursor = encodeCursor(last.getCreatedAt(), last.getProductId());
        }
        return new CursorPage<>(items, nextCursor, hasNext);
    }

    @Override
    public ProductQueryDto findById(Long productId) {
        return productQueryMapper.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
    
    private String encodeCursor(LocalDateTime createdAt, Long productId) {
        String raw = createdAt.toString() + "|" + productId;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
    
    private void decodeCursor(ProductSearchCondition cond, String cursor) {
        if (cursor == null || cursor.isBlank()) return;               // 첫 페이지
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|");
            cond.setCursorCreatedAt(LocalDateTime.parse(parts[0]));
            cond.setCursorProductId(Long.parseLong(parts[1]));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }
}
