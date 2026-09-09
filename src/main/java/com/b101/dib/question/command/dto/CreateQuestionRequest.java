package com.b101.dib.question.command.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateQuestionRequest {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    private String content;
}