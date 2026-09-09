package com.b101.dib.question.command.service;

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
}