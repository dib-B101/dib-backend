package com.b101.dib.bookmark.query.service;

import java.util.List;

import com.b101.dib.bookmark.query.dto.BookmarkQueryDto;

public interface BookmarkQueryService {

	List<BookmarkQueryDto> findAll();

	List<BookmarkQueryDto> findByMemberId(Long myId);

}
