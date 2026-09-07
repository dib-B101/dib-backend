package com.b101.dib.product.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPage<T> {
    private List<T> items;
    private String nextCursor;
    private boolean hasNext;
}