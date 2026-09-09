package com.b101.dib.question.query.service;

import com.b101.dib.question.query.dto.AdminQuestionQueryDto;
import com.b101.dib.question.query.dto.QuestionDetailDto;
import com.b101.dib.question.query.dto.QuestionQueryDto;

import java.util.List;

public interface QuestionQueryService {
    List<QuestionQueryDto> findMine(Long memberId, Boolean answered);
    QuestionDetailDto findById(Long memberId, Long questionId);
    List<AdminQuestionQueryDto> findAll(Boolean answered);
}
