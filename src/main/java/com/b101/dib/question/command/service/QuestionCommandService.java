package com.b101.dib.question.command.service;

import com.b101.dib.question.command.dto.CreateQuestionRequest;

public interface QuestionCommandService {
    Long create(Long memberId, CreateQuestionRequest request);
}