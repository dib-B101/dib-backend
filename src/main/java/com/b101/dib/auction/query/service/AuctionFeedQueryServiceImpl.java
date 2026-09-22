package com.b101.dib.auction.query.service;

import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.query.dto.AuctionCardRowDto;
import com.b101.dib.auction.query.dto.AuctionFeedSort;
import com.b101.dib.auction.query.dto.AuctionRecommendationDto;
import com.b101.dib.auction.repository.AuctionFeedMapper;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.Times;
import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastCardDto;
import com.b101.dib.liveBroadcast.repository.LiveFeedMapper;
import com.b101.dib.recommendation.command.dto.RecommendationItemDto;
import com.b101.dib.recommendation.command.service.RecommendationRefreshPublisher;
import com.b101.dib.recommendation.query.dto.RecommendationSnapshot;
import com.b101.dib.recommendation.repository.RecommendationCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionFeedQueryServiceImpl implements AuctionFeedQueryService {
    private static final int RECOMMEND_LIVE_MAX = 10;

    private final AuctionFeedMapper auctionFeedMapper;
    private final LiveFeedMapper liveFeedMapper;
    private final RecommendationCache recommendationCache;
    private final RecommendationRefreshPublisher recommendationRefreshPublisher;

    // LATEST 는 auction_id 커서, 다른 정렬은 offset 커서 (숫자 문자열 하나로 프론트 계약 유지)
    @Override
    public CursorPageDto<AuctionCardDto> findCards(Long memberId, String scope, String status, Long categoryId,
                                                   Long minPrice, Long maxPrice, String sort, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        AuctionFeedSort feedSort = AuctionFeedSort.from(sort);
        Long cursorValue = CursorPageDto.parseCursor(cursor);
        Long cursorId = feedSort == AuctionFeedSort.LATEST ? cursorValue : null;
        int offset = feedSort == AuctionFeedSort.LATEST || cursorValue == null ? 0 : cursorValue.intValue();

        List<AuctionCardRowDto> rows = auctionFeedMapper.findCards(memberId, normalizeScope(scope), normalizeStatus(status),
                categoryId, minPrice, maxPrice, feedSort.name(), cursorId, offset, limit + 1);
        List<AuctionCardDto> cards = rows.stream().map(r -> AuctionCardDto.from(r, memberId)).toList();
        CursorPageDto<AuctionCardDto> page = CursorPageDto.of(cards, limit, AuctionCardDto::getAuctionId);
        if (feedSort != AuctionFeedSort.LATEST && page.isHasNext()) {
            page.setNextCursor(String.valueOf(offset + limit));
        }
        return page;
    }

    // 커서는 bookmark_id (찜한 시각 순서). 경매 id 로 잡으면 오래전 찜이 최근 경매 뒤로 밀린다
    @Override
    public CursorPageDto<AuctionCardDto> findBookmarkedCards(Long memberId, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<AuctionCardRowDto> rows = auctionFeedMapper.findBookmarkedCards(memberId,
                CursorPageDto.parseCursor(cursor), limit + 1);
        CursorPageDto<AuctionCardRowDto> rowPage = CursorPageDto.of(rows, limit, AuctionCardRowDto::getBookmarkId);
        CursorPageDto<AuctionCardDto> page = new CursorPageDto<>();
        page.setItems(rowPage.getItems().stream().map(r -> AuctionCardDto.from(r, memberId)).toList());
        page.setNextCursor(rowPage.getNextCursor());
        page.setHasNext(rowPage.isHasNext());
        return page;
    }

    // LIVE 방송 카드는 기존 정책을 유지하고, 일반 경매는 AI 캐시 순서를 우선한다.
    // 캐시 미스·Redis/AI 장애 때는 마감 임박 순으로 즉시 폴백해 홈 요청을 실패시키지 않는다.
    @Override
    public AuctionRecommendationDto recommend(Long memberId, int size) {
        int limit = CursorPageDto.limit(size);
        Long recommendationMemberId = memberId == null ? 0L : memberId;
        RecommendationSnapshot snapshot = recommendationCache.get(recommendationMemberId)
                .or(() -> recommendationMemberId == 0L ? java.util.Optional.empty() : recommendationCache.get(0L))
                .orElse(null);
        if (snapshot == null || !recommendationMemberId.equals(snapshot.getMemberId())) {
            recommendationRefreshPublisher.requestIfNeeded(recommendationMemberId);
        }

        AuctionRecommendationDto dto = new AuctionRecommendationDto();
        dto.setLiveItems(liveFeedMapper.findLive(RECOMMEND_LIVE_MAX).stream().map(LiveBroadcastCardDto::from).toList());
        List<AuctionCardDto> cards = mergeAiOrderWithFallback(memberId, snapshot, limit + 1);
        boolean hasNext = cards.size() > limit;
        dto.setGeneralItems(hasNext ? new ArrayList<>(cards.subList(0, limit)) : cards);
        dto.setHasNext(hasNext);
        dto.setNextCursor(hasNext ? String.valueOf(limit) : null);
        dto.setServerTime(Times.now());
        return dto;
    }

    private List<AuctionCardDto> mergeAiOrderWithFallback(Long memberId, RecommendationSnapshot snapshot, int targetSize) {
        List<AuctionCardDto> ordered = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        if (snapshot != null && !snapshot.getItems().isEmpty()) {
            List<Long> ids = snapshot.getItems().stream()
                    .map(RecommendationItemDto::getAuctionId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            if (!ids.isEmpty()) {
                Map<Long, AuctionCardRowDto> rowsById = new HashMap<>();
                for (AuctionCardRowDto row : auctionFeedMapper.findActiveGeneralCardsByIds(memberId, ids)) {
                    rowsById.put(row.getAuctionId(), row);
                }
                for (Long id : ids) {
                    AuctionCardRowDto row = rowsById.get(id);
                    if (row != null && seen.add(id)) {
                        ordered.add(AuctionCardDto.from(row, memberId));
                    }
                }
            }
        }

        if (ordered.size() < targetSize) {
            List<AuctionCardRowDto> fallback = auctionFeedMapper.findCards(memberId, "GENERAL", "ACTIVE", null, null, null,
                    AuctionFeedSort.ENDING_SOON.name(), null, 0, targetSize);
            for (AuctionCardRowDto row : fallback) {
                if (seen.add(row.getAuctionId())) {
                    ordered.add(AuctionCardDto.from(row, memberId));
                    if (ordered.size() >= targetSize) {
                        break;
                    }
                }
            }
        }
        return ordered;
    }

    @Override
    public AuctionCardDto findCard(Long memberId, Long auctionId) {
        AuctionCardRowDto row = auctionFeedMapper.findCardById(memberId, auctionId);
        if (row == null) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_FOUND);
        }
        return AuctionCardDto.from(row, memberId);
    }

    private static String normalizeScope(String scope) {
        if (scope == null || scope.isBlank()) {
            return "ALL";
        }
        String s = scope.trim().toUpperCase();
        return s.equals("LIVE") || s.equals("GENERAL") ? s : "ALL";
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        String s = status.trim().toUpperCase();
        return s.equals("ALL") ? null : s;
    }
}
