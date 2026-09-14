package com.b101.dib.liveBroadcast.command.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRequest {
	String title;
	String description;
	LocalDateTime startedAt;
}
