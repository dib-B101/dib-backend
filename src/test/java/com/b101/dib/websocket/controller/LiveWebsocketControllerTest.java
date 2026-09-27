package com.b101.dib.websocket.controller;

import com.b101.dib.auction.repository.AuctionFeedMapper;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.liveChatting.command.service.LiveChattingService;
import com.b101.dib.liveChatting.domain.LiveChatting;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.websocket.dto.SendLiveChatCommand;
import com.b101.dib.websocket.service.LiveViewerCounter;
import com.b101.dib.websocket.service.LiveWebSocketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveWebsocketControllerTest {
    @Mock LiveChattingService liveChattingService;
    @Mock LiveBroadcastRepository liveBroadcastRepository;
    @Mock MemberRepository memberRepository;
    @Mock AuctionFeedMapper auctionFeedMapper;
    @Mock LiveWebSocketService liveWebSocketService;
    @Mock LiveViewerCounter liveViewerCounter;
    @InjectMocks LiveWebsocketController controller;

    @Test
    void acceptedAndBroadcastMessagesUseAuthenticatedAuthor() {
        when(liveBroadcastRepository.findById(42L)).thenReturn(Optional.of(
                LiveBroadcast.builder().liveBroadcastId(42L).status(LiveBroadcastStatus.LIVE).build()));
        when(liveChattingService.create(eq(7L), eq(42L), any())).thenReturn(
                LiveChatting.builder().liveChattingId(91L).memberId(7L).content("안녕하세요")
                        .time(LocalDateTime.of(2026, 9, 25, 12, 0)).build());
        when(memberRepository.findById(7L)).thenReturn(Optional.of(
                Member.builder().id(7L).nickname("윤정").build()));

        controller.send(42L, new SendLiveChatCommand("command-1", "안녕하세요"), () -> "7");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> accepted = ArgumentCaptor.forClass(Map.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> created = ArgumentCaptor.forClass(Map.class);
        verify(liveWebSocketService).sendToMember(eq(7L), eq("CHAT_ACCEPTED"), eq("command-1"), accepted.capture());
        verify(liveWebSocketService).broadcast(eq(42L), eq("LIVE_CHAT_MESSAGE_CREATED"), created.capture());
        assertEquals("7", accepted.getValue().get("memberId"));
        assertEquals("윤정", accepted.getValue().get("nickname"));
        assertEquals(accepted.getValue().get("memberId"), created.getValue().get("memberId"));
        assertEquals(accepted.getValue().get("nickname"), created.getValue().get("nickname"));
    }
}
