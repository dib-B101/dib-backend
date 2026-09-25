package com.b101.dib.websocket.controller;

import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.query.dto.AuctionCardRowDto;
import com.b101.dib.auction.repository.AuctionFeedMapper;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.common.util.Times;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;
import com.b101.dib.liveBroadcast.repository.LiveBroadcastRepository;
import com.b101.dib.liveChatting.command.dto.LiveChattingCommandDto;
import com.b101.dib.liveChatting.command.service.LiveChattingService;
import com.b101.dib.liveChatting.domain.LiveChatting;
import com.b101.dib.member.domain.Member;
import com.b101.dib.member.repository.MemberRepository;
import com.b101.dib.websocket.dto.SendLiveChatCommand;
import com.b101.dib.websocket.dto.SocketEnvelope;
import com.b101.dib.websocket.service.LiveViewerCounter;
import com.b101.dib.websocket.service.LiveWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// STOMP. 구독 /topic/live/{id} (LIVE_CHAT_MESSAGE_CREATED, LIVE_STARTED/ENDED, LIVE_AUCTION_*, LIVE_VIEWER_COUNT_UPDATED)
//        /user/queue/live (CHAT_ACCEPTED / CHAT_REJECTED)
//        전송 /app/live/{id}/messages {commandId, content}
//        스냅샷 /app/live/{id}/snapshot 구독하면 LIVE_SNAPSHOT 1회
@Controller
@RequiredArgsConstructor
public class LiveWebsocketController {
    private static final int MAX_CONTENT_LENGTH = 500;

    private final LiveChattingService liveChattingService;
    private final LiveBroadcastRepository liveBroadcastRepository;
    private final MemberRepository memberRepository;
    private final AuctionFeedMapper auctionFeedMapper;
    private final LiveWebSocketService liveWebSocketService;
    private final LiveViewerCounter liveViewerCounter;

    @MessageMapping("/live/{liveBroadcastId}/messages")
    public void send(@DestinationVariable("liveBroadcastId") Long liveBroadcastId, SendLiveChatCommand command, Principal principal) {
        Long memberId = memberId(principal);
        if (memberId == null) {
            return;   // CONNECT 에서 인증 안 된 세션 — 개인 큐도 없으니 무시
        }
        try {
            String content = command.getContent() == null ? "" : command.getContent().trim();
            if (content.isEmpty() || content.length() > MAX_CONTENT_LENGTH) {
                throw new BusinessException(ErrorCode.LIVE_CHATTING_INVALID_CONTENT);
            }
            LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(liveBroadcastId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_FOUND));
            if (liveBroadcast.getStatus() != LiveBroadcastStatus.LIVE) {
                throw new BusinessException(ErrorCode.LIVE_BROADCAST_NOT_LIVE);
            }

            LiveChattingCommandDto dto = LiveChattingCommandDto.builder()
                    .content(content)
                    .time(LocalDateTime.now())
                    .build();
            LiveChatting saved = liveChattingService.create(memberId, liveBroadcastId, dto);
            String authorNickname = nickname(memberId);

            Map<String, Object> accepted = new HashMap<>();
            accepted.put("commandId", command.getCommandId());
            accepted.put("liveBroadcastId", String.valueOf(liveBroadcastId));
            accepted.put("liveChattingId", String.valueOf(saved.getLiveChattingId()));
            accepted.put("memberId", String.valueOf(memberId));
            accepted.put("nickname", authorNickname);
            accepted.put("time", Times.iso(saved.getTime()));
            liveWebSocketService.sendToMember(memberId, "CHAT_ACCEPTED", command.getCommandId(), accepted);

            Map<String, Object> created = new HashMap<>();
            created.put("liveChattingId", String.valueOf(saved.getLiveChattingId()));
            created.put("liveBroadcastId", String.valueOf(liveBroadcastId));
            created.put("memberId", String.valueOf(memberId));
            created.put("nickname", authorNickname);
            created.put("content", saved.getContent());
            created.put("time", Times.iso(saved.getTime()));
            liveWebSocketService.broadcast(liveBroadcastId, "LIVE_CHAT_MESSAGE_CREATED", created);
        } catch (BusinessException e) {
            Map<String, Object> rejected = new HashMap<>();
            rejected.put("commandId", command.getCommandId());
            rejected.put("liveBroadcastId", String.valueOf(liveBroadcastId));
            rejected.put("code", e.getErrorCode().name());
            rejected.put("message", e.getErrorCode().getMessage());
            rejected.put("retryable", false);
            liveWebSocketService.sendToMember(memberId, "CHAT_REJECTED", command.getCommandId(), rejected);
        }
    }

    @SubscribeMapping("/live/{liveBroadcastId}/snapshot")
    public SocketEnvelope snapshot(@DestinationVariable("liveBroadcastId") Long liveBroadcastId, Principal principal) {
        Long memberId = memberId(principal);
        Map<String, Object> payload = new HashMap<>();
        LiveBroadcast liveBroadcast = liveBroadcastRepository.findById(liveBroadcastId).orElse(null);
        if (liveBroadcast == null) {
            payload.put("liveBroadcastId", String.valueOf(liveBroadcastId));
            payload.put("code", ErrorCode.LIVE_BROADCAST_NOT_FOUND.name());
            return SocketEnvelope.of("ERROR", null, payload);
        }

        Map<String, Object> live = new HashMap<>();
        live.put("liveBroadcastId", String.valueOf(liveBroadcastId));
        live.put("title", liveBroadcast.getTitle());
        live.put("streamUrl", liveBroadcast.getLivekitRoomName());
        live.put("viewCount", liveBroadcast.getViewCount());
        live.put("status", liveBroadcast.getStatus() == null ? null : liveBroadcast.getStatus().name());
        payload.put("liveBroadcast", live);

        AuctionCardRowDto row = auctionFeedMapper.findActiveCardByLiveBroadcastId(memberId, liveBroadcastId);
        if (row != null) {
            AuctionCardDto card = AuctionCardDto.from(row, memberId);
            Map<String, Object> auction = new HashMap<>();
            auction.put("auctionId", String.valueOf(card.getAuctionId()));
            auction.put("currentPrice", card.getCurrentPrice());
            auction.put("startPrice", card.getStartPrice());
            auction.put("bidCount", card.getBidCount());
            auction.put("status", card.getStatus() == null ? null : card.getStatus().name());
            auction.put("endedAt", card.getScheduledEndAt());
            Map<String, Object> myBid = new HashMap<>();
            myBid.put("isHighestBidder", card.getMyBid() != null && card.getMyBid().isHighestBidder());
            auction.put("myBid", myBid);
            payload.put("activeAuction", auction);

            Map<String, Object> product = new HashMap<>();
            product.put("productId", String.valueOf(card.getProductId()));
            product.put("title", card.getTitle());
            product.put("thumbnailUrl", card.getProduct() == null ? null : card.getProduct().getThumbnailUrl());
            payload.put("product", product);
        }

        payload.put("viewerCount", liveViewerCounter.count(liveBroadcastId));
        payload.put("serverTime", Times.now());
        return SocketEnvelope.of("LIVE_SNAPSHOT", null, payload);
    }

    private String nickname(Long memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);
        return member == null ? null : member.getNickname();
    }

    private static Long memberId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return null;
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
