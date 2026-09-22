package com.b101.dib.question.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminQuestionQueryDto {
    private Long questionId;
    private Long memberId;
    private String memberNickname;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String answer;
    private LocalDateTime answeredAt;
}
