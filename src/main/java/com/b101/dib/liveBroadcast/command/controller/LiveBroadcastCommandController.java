package com.b101.dib.liveBroadcast.command.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.liveBroadcast.command.dto.CreateRequest;
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
	private final LiveKitService liveKitService;
	
	@PostMapping("/{liveBroadcastId}/token")
    public ResponseEntity<Map<String, Object>> issueToken(
            @PathVariable("liveBroadcastId") Long liveBroadcastId
    ) {
		Long myId = 1L;
		LiveKitTokenResponse response = liveKitService.issueToken(myId, liveBroadcastId);
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
			@RequestBody CreateRequest request
			){
		Long myId = 1L;
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
			@PathVariable("liveBroadcastId") Long liveBroadcastId,
			@RequestBody UpdateRequest request
			){
		Long myId = 1L;
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.update(myId, liveBroadcastId, request);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 수정 성공");
		map.put("data", liveBroadcast);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}

	@Transactional
	@DeleteMapping("/{liveBroadcastId}")
	public ResponseEntity<Map<String, Object>> delete(
			@PathVariable("liveBroadcastId") Long liveBroadcastId
			){
		Long myId = 1L;
		LiveBroadcast liveBroadcast = liveBroadcastCommandService.delete(myId, liveBroadcastId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 삭제 성공");
		map.put("data", liveBroadcast);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
	

}
