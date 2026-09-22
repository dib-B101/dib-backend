package com.b101.dib.liveChatting.command.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.liveChatting.command.dto.LiveChattingCommandDto;
import com.b101.dib.liveChatting.command.service.LiveChattingService;
import com.b101.dib.auth.token.AccessTokenClaims;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/live-broadcasts/{liveBroadcastId}/chats")
@RequiredArgsConstructor
public class LiveChattingController {
	
	private final LiveChattingService liveChattingService;
	
	@PostMapping
	public ResponseEntity<Map<String, Object>> create(
			@AuthenticationPrincipal AccessTokenClaims claims,
			@PathVariable("liveBroadcastId") Long liveBroadcastId,
			@RequestBody LiveChattingCommandDto dto
			){
		liveChattingService.create(claims.memberId(), liveBroadcastId, dto);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 채팅 생성 성공");
		map.put("data", dto);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(map);
	}

}
