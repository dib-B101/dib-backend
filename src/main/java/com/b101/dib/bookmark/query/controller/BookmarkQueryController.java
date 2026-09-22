package com.b101.dib.bookmark.query.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.b101.dib.auction.query.dto.AuctionCardDto;
import com.b101.dib.auction.query.service.AuctionFeedQueryService;
import com.b101.dib.bookmark.query.dto.BookmarkQueryDto;
import com.b101.dib.bookmark.query.service.BookmarkQueryService;
import com.b101.dib.common.dto.CursorPageDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkQueryController {
	
	private final BookmarkQueryService bookamrkQueryService;
	private final AuctionFeedQueryService auctionFeedQueryService;
	
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
	
	// 찜 목록 화면이 그대로 그릴 수 있게 경매 카드로 내려준다 (productId 만 주면 프론트가 경매 목록과 교집합을 내야 해서 오래된 찜이 누락된다)
	@GetMapping("/me")
	public ResponseEntity<Map<String, Object>> findByMemberId(@RequestHeader("X-Member-Id") Long myId,
			@RequestParam(name = "cursor", required = false) String cursor,
			@RequestParam(name = "size", defaultValue = "30") int size){
		CursorPageDto<AuctionCardDto> page = auctionFeedQueryService.findBookmarkedCards(myId, cursor, size);
		HashMap<String, Object> map = new HashMap<>();
		map.put("message", "내 북마크 목록 조회 성공");
		map.put("data", page);
		return ResponseEntity.status(HttpStatus.OK).body(map);
	}
	

}
