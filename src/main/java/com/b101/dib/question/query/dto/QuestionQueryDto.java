package com.b101.dib.question.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class QuestionQueryDto {
    private Long questionId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;
}