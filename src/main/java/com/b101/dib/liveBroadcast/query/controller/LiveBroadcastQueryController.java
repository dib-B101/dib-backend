package com.b101.dib.liveBroadcast.query.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.liveBroadcast.query.dto.LiveBroadcastQueryDto;
import com.b101.dib.liveBroadcast.query.service.LiveBroadcastQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/live-broadcasts")
@RequiredArgsConstructor
public class LiveBroadcastQueryController {
	
	private final LiveBroadcastQueryService liveBroadcastQueryService;
	
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
			@PathVariable("liveBroadcastId") Long liveBroadcastId
			){
		LiveBroadcastQueryDto dto = liveBroadcastQueryService.findById(liveBroadcastId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "라이브 방송 조회 성공");
		map.put("data", dto);
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
