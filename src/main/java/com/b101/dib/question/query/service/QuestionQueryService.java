package com.b101.dib.question.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.question.query.dto.AdminQuestionQueryDto;
import com.b101.dib.question.query.dto.QuestionDetailDto;
import com.b101.dib.question.query.dto.QuestionQueryDto;


public interface QuestionQueryService {
    CursorPageDto<QuestionQueryDto> findMine(Long memberId, Boolean answered, String cursor, int size);
    QuestionDetailDto findById(Long memberId, Long questionId);
    CursorPageDto<AdminQuestionQueryDto> findAll(Boolean answered, String cursor, int size);
}
