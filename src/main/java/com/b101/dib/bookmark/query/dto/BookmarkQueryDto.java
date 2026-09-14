package com.b101.dib.bookmark.query.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkQueryDto {
	private Long bookmarkId;
	private Long memberId;
	private Long productId;
	private LocalDateTime createdAt;
}
