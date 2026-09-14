package com.b101.dib.bookmark.query.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.bookmark.query.dto.BookmarkQueryDto;
import com.b101.dib.bookmark.query.service.BookmarkQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkQueryController {
	
	private final BookmarkQueryService bookamrkQueryService;
	
	@GetMapping
	public ResponseEntity<Map<String, Object>> findAll(){
		List<BookmarkQueryDto> dtoList = bookamrkQueryService.findAll();
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "북마크 전체 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
	
	@GetMapping("/me")
	public ResponseEntity<Map<String, Object>> findByMemberId(){
		Long myId = 1L;
		List<BookmarkQueryDto> dtoList = bookamrkQueryService.findByMemberId(myId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "내 북마크 목록 조회 성공");
		map.put("data", dtoList);
		return ResponseEntity.status(HttpStatus.OK).body(map);
	}
	

}
