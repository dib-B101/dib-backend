package com.b101.dib.bookmark.command.service;

import com.b101.dib.bookmark.domain.Bookmark;

public interface BookmarkCommandService {

	Bookmark create(Long myId, Long productId);

	Bookmark delete(Long myId, Long productId);

}
