package com.b101.dib.liveBroadcast.command.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.auction.domain.Auction;
import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.repository.AuctionFeedMapper;
import com.b101.dib.liveBroadcast.command.dto.SetLiveItemsRequest;
import com.b101.dib.common.util.Times;
import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
import com.b101.dib.auth.token.AccessTokenClaims;
import com.b101.dib.liveBroadcast.command.dto.UpdateRequest;
import com.b101.dib.liveBroadcast.command.service.LiveBroadcastCommandService;
import com.b101.dib.liveBroadcast.domain.LiveBroadcast;
import com.b101.dib.liveBroadcast.domain.LiveBroadcastRole;
import com.b101.dib.websocket.livekit.config.LiveKitConfig;
import com.b101.dib.websocket.livekit.dto.LiveKitTokenResponse;
import com.b101.dib.websocket.livekit.service.LiveKitService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/live-broadcasts")
@RequiredArgsConstructor
public class LiveBroadcastCommandController {
	
	private final LiveBroadcastCommandService liveBroadcastCommandService;
	private final AuctionFeedMapper auctionFeedMapper;
	private final LiveKitService liveKitService;
	
	@PostMapping("/{liveBroadcastId}/token")
    public ResponseEntity<Map<String, Object>> issueToken(
            @AuthenticationPrincipal AccessTokenClaims claims,
            @PathVariable("liveBroadcastId") Long liveBroadcastId
    ) {
		LiveKitTokenResponse response = liveKitService.issueToken(claims.memberId(), liveBroadcastId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 토큰 발급 성공");
		map.put("response", response);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
    }
	
	@Transactional
	@PostMapping
	public ResponseEntity<Map<String, Object>> create(
			@RequestHeader("X-Member-Id") Long myId,
			@RequestBody CreateRequest request
			){
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.create(myId, request);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 생성 성공");
		map.put("data", liveBroadcast);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(map);
	}
	
	@Transactional
	@PatchMapping("/{liveBroadcastId}")
	public ResponseEntity<Map<String, Object>> update(
			@RequestHeader("X-Member-Id") Long myId,
			@PathVariable("liveBroadcastId") Long liveBroadcastId,
			@RequestBody UpdateRequest request
			){
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.update(myId, liveBroadcastId, request);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 수정 성공");
		map.put("data", liveBroadcast);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

	@PostMapping("/{liveBroadcastId}/start")
	public ResponseEntity<Map<String, Object>> start(
			@RequestHeader("X-Member-Id") Long myId,
			@PathVariable("liveBroadcastId") Long liveBroadcastId
			){
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.start(myId, liveBroadcastId);
		HashMap<String, Object> data = new HashMap<>();
		data.put("liveBroadcastId", String.valueOf(liveBroadcast.getLiveBroadcastId()));
		data.put("status", liveBroadcast.getStatus().name());
		data.put("startedAt", Times.iso(liveBroadcast.getStartedAt()));
		data.put("streamUrl", liveBroadcast.getLivekitRoomName());
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 시작 성공");
		map.put("data", data);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

	@PostMapping("/{liveBroadcastId}/end")
	public ResponseEntity<Map<String, Object>> end(
			@RequestHeader("X-Member-Id") Long myId,
			@PathVariable("liveBroadcastId") Long liveBroadcastId
			){
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.end(myId, liveBroadcastId);
		HashMap<String, Object> data = new HashMap<>();
		data.put("liveBroadcastId", String.valueOf(liveBroadcast.getLiveBroadcastId()));
		data.put("status", liveBroadcast.getStatus().name());
		data.put("endedAt", Times.iso(liveBroadcast.getEndedAt()));
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 종료 성공");
		map.put("data", data);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

	@PostMapping("/{liveBroadcastId}/auctions/{auctionId}/start")
	public ResponseEntity<Map<String, Object>> startLiveAuction(
			@RequestHeader("X-Member-Id") Long myId,
			@PathVariable("liveBroadcastId") Long liveBroadcastId,
			@PathVariable("auctionId") Long auctionId
			){
		Auction auction = liveBroadcastCommandService.startLiveAuction(myId, liveBroadcastId, auctionId);
		HashMap<String, Object> data = new HashMap<>();
		data.put("liveBroadcastId", String.valueOf(liveBroadcastId));
		data.put("auctionId", String.valueOf(auction.getAuctionId()));
		data.put("status", auction.getStatus().name());
		data.put("startedAt", Times.iso(auction.getStartedAt()));
		data.put("auctionTime", auction.getAuctionTime());
		data.put("scheduledEndAt", Times.iso(auction.getEndedAt()));
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 경매 시작 성공");
		map.put("data", data);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

	// 편성 = auction.live_broadcast_id 채우기. 전용 테이블이 없으므로 목록을 통째로 받아 그대로 맞춘다
	@Transactional
	@PutMapping("/{liveBroadcastId}/items")
	public ResponseEntity<Map<String, Object>> setItems(
			@RequestHeader("X-Member-Id") Long myId,
			@PathVariable("liveBroadcastId") Long liveBroadcastId,
			@RequestBody SetLiveItemsRequest request
			){
		liveBroadcastCommandService.setItems(myId, liveBroadcastId, request.getItems());
		HashMap<String, Object> data = new HashMap<>();
		data.put("liveBroadcastId", String.valueOf(liveBroadcastId));
		data.put("auctions", auctionFeedMapper.findCardsByLiveBroadcastId(myId, liveBroadcastId)
				.stream().map(row -> AuctionCardDto.from(row, myId)).toList());
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 상품 편성 성공");
		map.put("data", data);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

	@Transactional
	@DeleteMapping("/{liveBroadcastId}")
	public ResponseEntity<Map<String, Object>> delete(
			@RequestHeader("X-Member-Id") Long myId,
			@PathVariable("liveBroadcastId") Long liveBroadcastId
			){
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.delete(myId, liveBroadcastId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 삭제 성공");
		map.put("data", liveBroadcast);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
	

}
