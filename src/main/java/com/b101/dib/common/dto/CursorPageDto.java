package com.b101.dib.common.dto;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor
public class CursorPageDto<T> {
    public static final int MAX_SIZE = 100;

    private List<T> items;
    private String nextCursor;
    private boolean hasNext;

    // 커서는 마지막 행의 id. 요청 문자열 → Long (없으면 null = 첫 페이지)
    public static Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }

    public static int limit(int size) {
        return Math.max(1, Math.min(size, MAX_SIZE));
    }

    // limit + 1 개를 조회해 넘어온 rows 를 한 페이지로 자른다
    public static <T> CursorPageDto<T> of(List<T> rows, int limit, Function<T, Long> idOf) {
        boolean hasNext = rows.size() > limit;
        List<T> items = hasNext ? rows.subList(0, limit) : rows;
        CursorPageDto<T> page = new CursorPageDto<>();
        page.setItems(items);
        page.setHasNext(hasNext);
        page.setNextCursor(hasNext ? String.valueOf(idOf.apply(items.get(items.size() - 1))) : null);
        return page;
    }
}
