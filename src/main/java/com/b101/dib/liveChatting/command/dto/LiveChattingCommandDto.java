package com.b101.dib.liveChatting.command.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveChattingCommandDto {
	private String content;
	private LocalDateTime time;
}
