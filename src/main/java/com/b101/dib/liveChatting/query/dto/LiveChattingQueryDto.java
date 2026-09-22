package com.b101.dib.liveChatting.query.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveChattingQueryDto {
	private Long liveChattingId;
	private Long liveBroadcastId;
	private Long memberId;
	private String nickname;
	private String content;
	private LocalDateTime time; 

}
