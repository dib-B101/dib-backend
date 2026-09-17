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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionFeedQueryServiceImpl implements AuctionFeedQueryService {
    private static final int RECOMMEND_LIVE_MAX = 10;

    private final AuctionFeedMapper auctionFeedMapper;
    private final LiveFeedMapper liveFeedMapper;

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

    // 1차 추천: LIVE 방송 + 마감 임박 순 ACTIVE 일반 경매. AI 추천(명세 94/95) 붙으면 이 메서드만 교체
    @Override
    public AuctionRecommendationDto recommend(Long memberId, int size) {
        int limit = CursorPageDto.limit(size);
        AuctionRecommendationDto dto = new AuctionRecommendationDto();
        dto.setLiveItems(liveFeedMapper.findLive(RECOMMEND_LIVE_MAX).stream().map(LiveBroadcastCardDto::from).toList());
        List<AuctionCardRowDto> rows = auctionFeedMapper.findCards(memberId, "GENERAL", "ACTIVE", null, null, null,
                AuctionFeedSort.ENDING_SOON.name(), null, 0, limit + 1);
        List<AuctionCardDto> cards = rows.stream().map(r -> AuctionCardDto.from(r, memberId)).toList();
        CursorPageDto<AuctionCardDto> page = CursorPageDto.of(cards, limit, AuctionCardDto::getAuctionId);
        dto.setGeneralItems(page.getItems());
        dto.setHasNext(page.isHasNext());
        dto.setNextCursor(page.isHasNext() ? String.valueOf(limit) : null);
        dto.setServerTime(Times.now());
        return dto;
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
