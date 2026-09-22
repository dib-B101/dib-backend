package com.b101.dib.product.query.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 인기 검색어. 상품 검색에 들어온 키워드를 Redis ZSET(search:popular-keywords)에 횟수로 누적하고 상위 N개를 돌려준다.
 * 앱의 검색 화면이 첫 진입에 보여주던 고정 목록("빈티지 카메라" …)을 실제 검색 기록으로 바꾸기 위한 것이다.
 * Redis 장애는 검색을 막을 이유가 없으므로 기록 실패는 무시하고 조회 실패는 빈 목록으로 돌려준다.
 */
@Component
@Slf4j
public class SearchKeywordStore {
    static final String KEY = "search:popular-keywords";
    static final int MAX_KEYWORD_LENGTH = 30;
    // 검색어가 무한히 쌓이지 않게 하위 순위는 잘라낸다
    static final int MAX_ENTRIES = 1000;

    private final StringRedisTemplate redisTemplate;

    public SearchKeywordStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void record(String rawKeyword) {
        String keyword = normalize(rawKeyword);
        if (keyword == null) {
            return;
        }
        try {
            redisTemplate.opsForZSet().incrementScore(KEY, keyword, 1);
            redisTemplate.opsForZSet().removeRange(KEY, 0, -(MAX_ENTRIES + 1));
        } catch (Exception e) {
            log.warn("인기 검색어 기록 실패 keyword={} : {}", keyword, e.getMessage());
        }
    }

    public List<String> top(int size) {
        if (size <= 0) {
            return List.of();
        }
        try {
            Set<String> range = redisTemplate.opsForZSet().reverseRange(KEY, 0, size - 1L);
            return range == null ? List.of() : new ArrayList<>(range);
        } catch (Exception e) {
            log.warn("인기 검색어 조회 실패 : {}", e.getMessage());
            return List.of();
        }
    }

    // 앞뒤 공백 제거, 연속 공백은 하나로. 비었거나 너무 길면 기록하지 않는다
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String keyword = raw.trim().replaceAll("\\s+", " ");
        if (keyword.isEmpty() || keyword.length() > MAX_KEYWORD_LENGTH) {
            return null;
        }
        return keyword;
    }
}
