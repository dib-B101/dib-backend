package com.b101.dib.bookmark.command.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.bookmark.command.service.BookmarkCommandService;
import com.b101.dib.bookmark.domain.Bookmark;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products/{productId}")
@RequiredArgsConstructor
public class BookmarkCommandController {
	
	private final BookmarkCommandService bookmarkCommandService;
	
	@PostMapping("/bookmark")
	public ResponseEntity<Map<String, Object>> create(
			@PathVariable("productId") Long productId){
		Long myId = 1L;
		Bookmark bookmark = bookmarkCommandService.create(myId, productId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "북마크 생성 성공");
		map.put("bookmark", bookmark);
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(map);
	}
	
	@DeleteMapping("/bookmark")
	public ResponseEntity<Map<String, Object>> delete(
			@PathVariable("productId") Long productId){
		Long myId = 1L;
		Bookmark bookmark = bookmarkCommandService.delete(myId, productId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "북마크 삭제 성공");
		map.put("bookmark", bookmark);
		return ResponseEntity
				.status(HttpStatus.OK)
				.body(map);
	}
}
