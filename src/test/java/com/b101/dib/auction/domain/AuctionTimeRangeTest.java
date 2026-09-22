package com.b101.dib.auction.domain;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 경매 시간 허용 범위는 라이브 편성 여부로 갈린다.
//   일반  — 5분 이상 (상한 없음)
//   라이브 — 30초 ~ 5분. 방송 중 한 점씩 파는 자리라 5분짜리면 한 시간에 12점이 한계고,
//            반대로 상한이 없으면 방송이 물건 하나에 묶인다
class AuctionTimeRangeTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 21, 20, 0);

    private static Auction auction(Long liveBroadcastId, Integer auctionTime) {
        Auction auction = Auction.scheduled(1L, 10_000L, auctionTime, NOW);
        auction.setLiveBroadcastId(liveBroadcastId);
        return auction;
    }

    @Test
    void regularAuctionRejectsAnythingUnderFiveMinutes() {
        Auction regular = auction(null, null);

        assertThatThrownBy(() -> regular.validateAuctionTime(299))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUCTION_SCHEDULE_INVALID);
        assertThatCode(() -> regular.validateAuctionTime(300)).doesNotThrowAnyException();
    }

    @Test
    void regularAuctionHasNoUpperBound() {
        Auction regular = auction(null, null);

        assertThatCode(() -> regular.validateAuctionTime(86_400)).doesNotThrowAnyException();
    }

    @Test
    void liveAuctionAllowsThirtySeconds() {
        Auction live = auction(7L, null);

        assertThatCode(() -> live.validateAuctionTime(30)).doesNotThrowAnyException();
        assertThatCode(() -> live.validateAuctionTime(300)).doesNotThrowAnyException();
    }

    @Test
    void liveAuctionRejectsOutsideThirtySecondsToFiveMinutes() {
        Auction live = auction(7L, null);

        assertThatThrownBy(() -> live.validateAuctionTime(29))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUCTION_SCHEDULE_INVALID);
        assertThatThrownBy(() -> live.validateAuctionTime(301))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUCTION_SCHEDULE_INVALID);
    }

    @Test
    void rejectsMissingAuctionTime() {
        assertThatThrownBy(() -> auction(null, null).validateAuctionTime(null))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> auction(7L, null).validateAuctionTime(null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void startsThirtySecondLiveAuction() {
        Auction live = auction(7L, 30);

        live.start(NOW);

        assertThat(live.getStatus()).isEqualTo(AuctionStatus.ACTIVE);
        assertThat(Duration.between(NOW, live.getEndedAt()).getSeconds()).isEqualTo(30);
    }

    @Test
    void stillRejectsThirtySecondsWhenNotScheduledForLive() {
        Auction regular = auction(null, 30);

        assertThatThrownBy(() -> regular.start(NOW))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.AUCTION_SCHEDULE_INVALID);
    }
}
