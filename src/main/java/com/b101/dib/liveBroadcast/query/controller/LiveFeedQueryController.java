package com.b101.dib.liveBroadcast.query.controller;

import com.b101.dib.liveBroadcast.query.service.LiveFeedQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// 홈 라이브 피드 (프론트 LiveRemoteDataSource.getFeed). LiveBroadcastQueryController 와 분리 — 라이브 담당 파일 무수정
@RestController
@RequestMapping("/api/v1/live-broadcasts")
@RequiredArgsConstructor
public class LiveFeedQueryController {
    private final LiveFeedQueryService liveFeedQueryService;

    @GetMapping("/feed")
    public ResponseEntity<Map<String, Object>> feed(
            @RequestHeader(value = "X-Member-Id", required = false) Long memberId,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "라이브 피드 조회 성공");
        map.put("data", liveFeedQueryService.feed(memberId, cursor, size));
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
