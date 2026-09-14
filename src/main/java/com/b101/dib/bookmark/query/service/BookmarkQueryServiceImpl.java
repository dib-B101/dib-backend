package com.b101.dib.bookmark.query.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.b101.dib.bookmark.query.dto.BookmarkQueryDto;
import com.b101.dib.bookmark.repository.BookmarkMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkQueryServiceImpl implements BookmarkQueryService{
	
	private final BookmarkMapper bookmarkMapper;

	@Override
	public List<BookmarkQueryDto> findAll() {
		return bookmarkMapper.findAll();
	}

	@Override
	public List<BookmarkQueryDto> findByMemberId(Long myId) {
		return bookmarkMapper.findByMemberId(myId);
	}

}
