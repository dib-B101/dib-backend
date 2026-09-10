package com.b101.dib.question.command.service;

import com.b101.dib.question.command.dto.AnswerQuestionRequest;
import com.b101.dib.question.command.dto.CreateQuestionRequest;

import java.time.LocalDateTime;

public interface QuestionCommandService {
    Long create(Long memberId, CreateQuestionRequest request);
    LocalDateTime answer(Long questionId, AnswerQuestionRequest request);
}
