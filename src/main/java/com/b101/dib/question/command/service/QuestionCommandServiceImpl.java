package com.b101.dib.question.command.service;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;

    @Override
    public Question create(Long memberId, CreateQuestionRequest request) {
        Question question = Question.builder()
                .memberId(memberId)
                .title(request.getTitle())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();
        return questionRepository.save(question);
    }

    @Override
    public LocalDateTime answer(Long questionId, AnswerQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        question.setAnswer(request.getAnswer());
        question.setAnsweredAt(now);
        notificationRepository.save(Notification.system(question.getMemberId(), "문의 답변 완료",
                "문의 #" + questionId + "에 관리자 답변이 등록되었습니다."));
        return now;
    }
}
