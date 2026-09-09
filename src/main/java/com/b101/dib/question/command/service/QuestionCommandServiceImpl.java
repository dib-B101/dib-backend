package com.b101.dib.question.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.question.command.dto.AnswerQuestionRequest;
import com.b101.dib.question.command.dto.CreateQuestionRequest;
import com.b101.dib.question.domain.Question;
import com.b101.dib.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionCommandServiceImpl implements QuestionCommandService {
    private final QuestionRepository questionRepository;

    @Override
    public Long create(Long memberId, CreateQuestionRequest request) {
        Question question = Question.builder()
                .memberId(memberId)
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        return questionRepository.save(question).getQuestionId();
    }

    @Override
    public LocalDateTime answer(Long questionId, AnswerQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        question.setAnswer(request.getAnswer());
        question.setAnsweredAt(now);
        return now;
    }
}
