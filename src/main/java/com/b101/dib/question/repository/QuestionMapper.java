package com.b101.dib.question.repository;

import com.b101.dib.question.query.dto.QuestionDetailDto;
import com.b101.dib.question.query.dto.QuestionQueryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper {
    List<QuestionQueryDto> findByMemberId(@Param("memberId") Long memberId,
                                          @Param("answered") Boolean answered);
    QuestionDetailDto findById(@Param("questionId") Long questionId);
}