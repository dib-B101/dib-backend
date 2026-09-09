package com.b101.dib.question.repository;

import com.b101.dib.question.domain.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}