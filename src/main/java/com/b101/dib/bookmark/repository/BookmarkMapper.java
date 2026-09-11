package com.b101.dib.bookmark.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.b101.dib.bookmark.query.dto.BookmarkQueryDto;

@Mapper
public interface BookmarkMapper {

	List<BookmarkQueryDto> findAll();

	List<BookmarkQueryDto> findByMemberId(Long memberId);

}
