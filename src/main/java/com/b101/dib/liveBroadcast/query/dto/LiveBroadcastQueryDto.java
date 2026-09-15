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
	String streamUrl;
	LocalDateTime startedAt;
	LocalDateTime endedAt;
	Integer viewCount;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
}
