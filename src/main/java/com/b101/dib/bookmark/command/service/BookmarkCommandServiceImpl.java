package com.b101.dib.bookmark.command.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.b101.dib.bookmark.domain.Bookmark;
import com.b101.dib.bookmark.repository.BookmarkRepository;
import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookmarkCommandServiceImpl implements BookmarkCommandService {
	
	private final BookmarkRepository bookmarkRepository;
	
	@Override
	public Bookmark create(Long myId, Long productId) {
		Bookmark bookmark = bookmarkRepository.findByMemberIdAndProductId(myId, productId);
		if(bookmark != null) {
			throw new BusinessException(ErrorCode.BOOKMARK_ALREADY_EXISTS);
		}
		bookmark = Bookmark.builder()
				.memberId(myId)
				.productId(productId)
				.createdAt(LocalDateTime.now())
				.build();
		bookmarkRepository.save(bookmark);
		return bookmark;
	}

	@Override
	public Bookmark delete(Long myId, Long productId) {
		Bookmark bookmark = bookmarkRepository.findByMemberIdAndProductId(myId, productId);
		if(bookmark == null) {
			throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
		}
		bookmarkRepository.delete(bookmark);
		return bookmark;
	}
}
