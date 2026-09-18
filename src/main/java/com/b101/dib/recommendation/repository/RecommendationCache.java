package com.b101.dib.recommendation.repository;

import com.b101.dib.common.ai.AiServerProperties;
import com.b101.dib.common.util.Times;
import com.b101.dib.recommendation.command.dto.RecommendationCallbackRequest;
import com.b101.dib.recommendation.query.dto.RecommendationSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationCache {
    private static final String SNAPSHOT_PREFIX = "ai:recommendation:snapshot:";
    private static final String REFRESH_PREFIX = "ai:recommendation:refresh:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AiServerProperties properties;

    public Optional<RecommendationSnapshot> get(Long memberId) {
        try {
            String raw = redisTemplate.opsForValue().get(snapshotKey(memberId));
            return raw == null ? Optional.empty() : Optional.of(objectMapper.readValue(raw, RecommendationSnapshot.class));
        } catch (Exception e) {
            log.warn("AI 추천 캐시 조회 실패 memberId={} — 폴백 사용", memberId, e);
            return Optional.empty();
        }
    }

    public boolean beginRefresh(Long memberId, String jobId) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                    refreshKey(memberId), jobId, properties.getRecommendationRefreshLockTtl()));
        } catch (Exception e) {
            log.warn("AI 추천 갱신 잠금 실패 memberId={}", memberId, e);
            return false;
        }
    }

    public boolean saveIfCurrent(RecommendationCallbackRequest request) {
        Long memberId = normalize(request.getMemberId());
        try {
            String pendingJobId = redisTemplate.opsForValue().get(refreshKey(memberId));
            if (pendingJobId != null && !pendingJobId.equals(request.getJobId())) {
                log.info("늦게 도착한 AI 추천 콜백 무시 memberId={} jobId={} currentJobId={}",
                        memberId, request.getJobId(), pendingJobId);
                return false;
            }
            RecommendationSnapshot snapshot = new RecommendationSnapshot();
            snapshot.setJobId(request.getJobId());
            snapshot.setMemberId(memberId);
            snapshot.setCachedAt(Times.iso(LocalDateTime.now()));
            snapshot.setItems(request.getItems());
            redisTemplate.opsForValue().set(
                    snapshotKey(memberId), objectMapper.writeValueAsString(snapshot), properties.getRecommendationCacheTtl());
            redisTemplate.delete(refreshKey(memberId));
            return true;
        } catch (Exception e) {
            log.warn("AI 추천 캐시 저장 실패 memberId={} jobId={}", memberId, request.getJobId(), e);
            return false;
        }
    }

    public void endRefresh(Long memberId, String jobId) {
        Long normalized = normalize(memberId);
        try {
            String current = redisTemplate.opsForValue().get(refreshKey(normalized));
            if (jobId.equals(current)) {
                redisTemplate.delete(refreshKey(normalized));
            }
        } catch (Exception e) {
            log.debug("AI 추천 갱신 잠금 정리 실패 memberId={} jobId={}", normalized, jobId, e);
        }
    }

    private static Long normalize(Long memberId) {
        return memberId == null ? 0L : memberId;
    }

    private static String snapshotKey(Long memberId) {
        return SNAPSHOT_PREFIX + normalize(memberId);
    }

    private static String refreshKey(Long memberId) {
        return REFRESH_PREFIX + normalize(memberId);
    }
}
