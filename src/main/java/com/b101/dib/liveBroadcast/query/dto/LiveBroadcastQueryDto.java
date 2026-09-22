package com.b101.dib.liveBroadcast.query.dto;

import java.time.LocalDateTime;

import com.b101.dib.liveBroadcast.domain.LiveBroadcastStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveBroadcastQueryDto {
	Long liveBroadcastId;
	Long memberId;
	String title;
	String description;
	LiveBroadcastStatus status;
	String livekitRoomName;
	LocalDateTime startedAt;
	LocalDateTime endedAt;
	Integer viewCount;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
	// 홈 LIVE 카드용 대표 상품. 편성 상품 중 첫 번째(auction_id 순)의 제목·썸네일과 편성 개수
	Integer itemCount;
	String firstItemTitle;
	String firstItemThumbnailUrl;
}
