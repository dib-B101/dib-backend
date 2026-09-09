package com.b101.dib.question.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    private Long memberId;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    private String answer;
    private LocalDateTime answeredAt;
}