package com.b101.dib.question.query.service;

import com.b101.dib.common.dto.CursorPageDto;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.question.query.dto.AdminQuestionQueryDto;
import com.b101.dib.question.query.dto.QuestionDetailDto;
import com.b101.dib.question.query.dto.QuestionQueryDto;
import com.b101.dib.question.repository.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionQueryServiceImpl implements QuestionQueryService {
    private final QuestionMapper questionMapper;

    @Override
    public CursorPageDto<QuestionQueryDto> findMine(Long memberId, Boolean answered, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<QuestionQueryDto> rows = questionMapper.findByMemberId(memberId, answered, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, QuestionQueryDto::getQuestionId);
    }

    @Override
    public QuestionDetailDto findById(Long memberId, Long questionId) {
        QuestionDetailDto dto = questionMapper.findById(questionId);
        if (dto == null) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND);
        }
        if (!dto.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return dto;
    }

    @Override
    public CursorPageDto<AdminQuestionQueryDto> findAll(Boolean answered, String cursor, int size) {
        int limit = CursorPageDto.limit(size);
        List<AdminQuestionQueryDto> rows = questionMapper.findAll(answered, CursorPageDto.parseCursor(cursor), limit + 1);
        return CursorPageDto.of(rows, limit, AdminQuestionQueryDto::getQuestionId);
    }
}
