package com.b101.dib.question.query.controller;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.question.query.dto.AdminQuestionQueryDto;
import com.b101.dib.question.query.service.QuestionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionQueryController {
    private final QuestionQueryService questionQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(@RequestParam(name = "answered", required = false) Boolean answered,
                                                       @RequestParam(name = "cursor", required = false) String cursor,
                                                       @RequestParam(name = "size", defaultValue = "20") int size) {
        CursorPageDto<AdminQuestionQueryDto> page = questionQueryService.findAll(answered, cursor, size);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "문의 전체 목록 조회 성공");
        map.put("data", page);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
