package com.b101.dib.liveBroadcast.query.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.auction.domain.AuctionStatus;
import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.repository.AuctionFeedMapper;
import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;
import com.b101.dib.liveBroadcast.query.service.LiveBroadcastQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/live-broadcasts")
@RequiredArgsConstructor
public class LiveBroadcastQueryController {
	
	private final LiveBroadcastQueryService liveBroadcastQueryService;
	private final AuctionFeedMapper auctionFeedMapper;
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> findAll(){
		List<LiveBroadcastQueryDto> dtoList = liveBroadcastQueryService.findAll();
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 전체 목록 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
	
	@GetMapping("/{liveBroadcastId}")
	public ResponseEntity<Map<String, Object>> findById(
			@PathVariable("liveBroadcastId") Long liveBroadcastId,
			@RequestHeader(value = "X-Member-Id", required = false) Long memberId
			){
		LiveBroadcastQueryDto dto = liveBroadcastQueryService.findById(liveBroadcastId);
		List<AuctionCardDto> auctions = auctionFeedMapper.findCardsByLiveBroadcastId(memberId, liveBroadcastId)
				.stream()
				.map(row -> AuctionCardDto.from(row, memberId))
				.toList();
		AuctionCardDto currentAuction = auctions.stream()
				.filter(auction -> auction.getStatus() == AuctionStatus.ACTIVE)
				.findFirst()
				.orElse(null);
		LinkedHashMap<String, Object> data = new LinkedHashMap<>();
		data.put("liveBroadcastId", dto.getLiveBroadcastId());
		data.put("memberId", dto.getMemberId());
		data.put("title", dto.getTitle());
		data.put("description", dto.getDescription());
		data.put("status", dto.getStatus());
		data.put("livekitRoomName", dto.getLivekitRoomName());
		data.put("startedAt", dto.getStartedAt());
		data.put("endedAt", dto.getEndedAt());
		data.put("viewCount", dto.getViewCount());
		data.put("createdAt", dto.getCreatedAt());
		data.put("updatedAt", dto.getUpdatedAt());
		data.put("auctions", auctions);
		data.put("currentAuction", currentAuction);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 조회 성공");
		map.put("data", data);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);		
	}
	
	@GetMapping("/members/{memberId}")
	public ResponseEntity<Map<String, Object>> findByMemberId(
			@PathVariable("memberId") Long memberId
			){
		List<LiveBroadcastQueryDto> dtoList = liveBroadcastQueryService.findByMemberId(memberId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "회원 라이브 방송 목록 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
}
