package com.b101.dib.question.command.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AnswerQuestionRequest {
    @NotBlank
    private String answer;
}