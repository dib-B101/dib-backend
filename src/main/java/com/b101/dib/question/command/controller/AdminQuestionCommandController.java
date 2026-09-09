package com.b101.dib.question.command.controller;

import com.b101.dib.question.command.dto.AnswerQuestionRequest;
import com.b101.dib.question.command.service.QuestionCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
public class AdminQuestionCommandController {
    private final QuestionCommandService questionCommandService;

    @PatchMapping("/{questionId}/answer")
    public ResponseEntity<Map<String, Object>> answer(@PathVariable("questionId") Long questionId,
                                                      @Valid @RequestBody AnswerQuestionRequest request) {
        LocalDateTime answeredAt = questionCommandService.answer(questionId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "문의 답변 등록 성공");
        map.put("questionId", questionId);
        map.put("answer", request.getAnswer());
        map.put("answeredAt", answeredAt);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
