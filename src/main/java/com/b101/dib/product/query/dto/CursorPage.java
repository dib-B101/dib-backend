package com.b101.dib.product.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPage<T> {
    private List<T> items;
    private String nextCursor;   // 다음 페이지 요청 시 그대로 보내는 값. 마지막 페이지면 null
    private boolean hasNext;
}
