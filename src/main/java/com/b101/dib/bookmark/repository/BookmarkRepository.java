package com.b101.dib.bookmark.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.b101.dib.bookmark.domain.Bookmark;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

	Bookmark findByMemberIdAndProductId(Long myId, Long productId);

}
