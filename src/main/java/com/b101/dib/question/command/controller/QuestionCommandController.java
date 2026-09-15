package com.b101.dib.question.command.controller;

import com.b101.dib.question.command.dto.CreateQuestionRequest;
import com.b101.dib.question.command.service.QuestionCommandService;
import com.b101.dib.question.domain.Question;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionCommandController {
    private final QuestionCommandService questionCommandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestHeader("X-Member-Id") Long memberId,
                                                      @Valid @RequestBody CreateQuestionRequest request) {
        Question question = questionCommandService.create(memberId, request);
        HashMap<String, Object> data = new HashMap<>();
        data.put("questionId", question.getQuestionId());
        data.put("createdAt", question.getCreatedAt());
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "문의 등록 성공");
        map.put("data", data);
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}