package com.b101.dib.question.query.controller;

import com.b101.dib.question.query.dto.QuestionDetailDto;
import com.b101.dib.question.query.dto.QuestionQueryDto;
import com.b101.dib.question.query.service.QuestionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionQueryController {
    private final QuestionQueryService questionQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findMine(@RequestHeader("X-Member-Id") Long memberId,
                                                        @RequestParam(name = "answered", required = false) Boolean answered) {
        List<QuestionQueryDto> dtoList = questionQueryService.findMine(memberId, answered);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "내 문의 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<Map<String, Object>> findById(@RequestHeader("X-Member-Id") Long memberId,
                                                        @PathVariable("questionId") Long questionId) {
        QuestionDetailDto dto = questionQueryService.findById(memberId, questionId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "문의 상세 조회 성공");
        map.put("data", dto);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}