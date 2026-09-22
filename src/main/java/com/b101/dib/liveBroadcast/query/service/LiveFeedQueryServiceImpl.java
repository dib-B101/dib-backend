package com.b101.dib.liveBroadcast.query.service;

import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.query.dto.AuctionCardRowDto;
import com.b101.dib.auction.repository.AuctionFeedMapper;
import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.util.Times;
import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastCardDto;
import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;
import com.b101.dib.liveBroadcast.query.dto.LiveFeedDto;
import com.b101.dib.liveBroadcast.query.dto.LiveFeedItemDto;
import com.b101.dib.liveBroadcast.repository.LiveFeedMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// 홈 라이브 피드: LIVE → SCHEDULED 순. 커서는 offset
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveFeedQueryServiceImpl implements LiveFeedQueryService {
    private static final int MAX_SIZE = 50;

    private final LiveFeedMapper liveFeedMapper;
    private final AuctionFeedMapper auctionFeedMapper;

    @Override
    public LiveFeedDto feed(Long memberId, String cursor, int size) {
        int limit = Math.max(1, Math.min(size, MAX_SIZE));
        Long cursorValue = CursorPageDto.parseCursor(cursor);
        int offset = cursorValue == null ? 0 : cursorValue.intValue();
        List<LiveBroadcastQueryDto> rows = liveFeedMapper.findFeed(offset, limit + 1);
        boolean hasNext = rows.size() > limit;
        List<LiveBroadcastQueryDto> pageRows = hasNext ? rows.subList(0, limit) : rows;

        List<LiveFeedItemDto> items = new ArrayList<>();
        for (LiveBroadcastQueryDto live : pageRows) {
            LiveFeedItemDto item = new LiveFeedItemDto();
            item.setLiveBroadcast(LiveBroadcastCardDto.from(live));
            AuctionCardRowDto row = auctionFeedMapper.findActiveCardByLiveBroadcastId(memberId, live.getLiveBroadcastId());
            if (row != null) {
                AuctionCardDto card = AuctionCardDto.from(row, memberId);
                item.setActiveAuction(card);
                item.setProduct(card.getProduct());
            }
            items.add(item);
        }
        LiveFeedDto dto = new LiveFeedDto();
        dto.setItems(items);
        dto.setHasNext(hasNext);
        dto.setNextCursor(hasNext ? String.valueOf(offset + limit) : null);
        dto.setServerTime(Times.now());
        return dto;
    }
}
