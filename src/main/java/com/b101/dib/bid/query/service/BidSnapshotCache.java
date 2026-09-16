package com.b101.dib.bid.query.service;

import com.b101.dib.bid.query.dto.BidSnapshotCacheEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

// 입찰 스냅샷 Hot State. 키 auction:{id}:snapshot, 값 JSON. Redis 장애는 전부 miss 로 취급해 DB 로 간다
@Component
@Slf4j
public class BidSnapshotCache {
    public static final Duration MIN_TTL = Duration.ofSeconds(60);
    public static final Duration AFTER_END_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public BidSnapshotCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                            @Value("${dib.cache.snapshot-enabled:true}") boolean enabled) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    public Optional<BidSnapshotCacheEntry> get(Long auctionId) {
        if (!enabled) {
            return Optional.empty();
        }
        try {
            String json = redisTemplate.opsForValue().get(key(auctionId));
            return json == null ? Optional.empty() : Optional.of(objectMapper.readValue(json, BidSnapshotCacheEntry.class));
        } catch (Exception e) {
            log.warn("스냅샷 캐시 조회 실패 auctionId={} : {}", auctionId, e.getMessage());
            return Optional.empty();
        }
    }

    public void put(Long auctionId, BidSnapshotCacheEntry entry, Duration ttl) {
        if (!enabled) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key(auctionId), objectMapper.writeValueAsString(entry), ttl.compareTo(MIN_TTL) < 0 ? MIN_TTL : ttl);
        } catch (Exception e) {
            log.warn("스냅샷 캐시 저장 실패 auctionId={} : {}", auctionId, e.getMessage());
        }
    }

    public void evict(Long auctionId) {
        if (!enabled) {
            return;
        }
        try {
            redisTemplate.delete(key(auctionId));
        } catch (Exception e) {
            log.warn("스냅샷 캐시 삭제 실패 auctionId={} : {}", auctionId, e.getMessage());
        }
    }

    private static String key(Long auctionId) {
        return "auction:" + auctionId + ":snapshot";
    }
}
