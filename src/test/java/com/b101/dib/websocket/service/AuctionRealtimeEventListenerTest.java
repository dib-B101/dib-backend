package com.b101.dib.websocket.service;

import com.b101.dib.auction.command.dto.AuctionEndResultDto;
import com.b101.dib.auction.command.service.AuctionEndedEvent;
import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.repository.AuctionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// 시청 화면은 LIVE_AUCTION_CLOSED 만 듣는다. 낙찰자 id 가 없으면 "내가 낙찰자인지" 를 알 수 없다
@ExtendWith(MockitoExtension.class)
class AuctionRealtimeEventListenerTest {

    @Mock AuctionWebSocketService auctionWebSocketService;
    @Mock AuctionRepository auctionRepository;
    @Mock LiveWebSocketService liveWebSocketService;
    @InjectMocks AuctionRealtimeEventListener listener;

    @Test
    void liveClosedCarriesWinnerIdAsString() {
        when(auctionRepository.findById(21L)).thenReturn(Optional.of(
                Auction.builder().auctionId(21L).liveBroadcastId(5L).build()));

        listener.onAuctionEnded(new AuctionEndedEvent(result(21L, "SOLD", 33L)));

        Map<String, Object> live = captureLivePayload();
        assertThat(live.get("winnerId")).isEqualTo("33");
        assertThat(live.get("result")).isEqualTo("SOLD");
        assertThat(live.get("liveBroadcastId")).isEqualTo("5");
    }

    @Test
    void liveClosedHasNullWinnerWhenUnsold() {
        when(auctionRepository.findById(21L)).thenReturn(Optional.of(
                Auction.builder().auctionId(21L).liveBroadcastId(5L).build()));

        listener.onAuctionEnded(new AuctionEndedEvent(result(21L, "UNSOLD", null)));

        assertThat(captureLivePayload()).containsEntry("winnerId", null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureLivePayload() {
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(liveWebSocketService).broadcast(eq(5L), eq("LIVE_AUCTION_CLOSED"), captor.capture());
        return captor.getValue();
    }

    private static AuctionEndResultDto result(long auctionId, String result, Long winnerId) {
        AuctionEndResultDto dto = new AuctionEndResultDto();
        dto.setAuctionId(auctionId);
        dto.setResult(result);
        dto.setFinalPrice(45_000L);
        dto.setWinnerId(winnerId);
        dto.setEndedAt("2026-09-22T10:00:00Z");
        return dto;
    }
}
