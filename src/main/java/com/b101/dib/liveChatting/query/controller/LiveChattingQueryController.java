package com.b101.dib.liveChatting.query.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.liveChatting.query.dto.LiveChattingQueryDto;
import com.b101.dib.liveChatting.query.service.LiveChattingQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/live-broadcasts/{liveBroadcastId}/chats")
@RequiredArgsConstructor
public class LiveChattingQueryController {
	
	private final LiveChattingQueryService liveChattingQueryService;
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> findByLiveBroadcastId(
			@PathVariable("liveBroadcastId") Long liveBroadcastId
			){
		List<LiveChattingQueryDto> dtoList = liveChattingQueryService.findByLiveBroadcastId(liveBroadcastId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 채팅 목록 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

}
