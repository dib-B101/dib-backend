package com.b101.dib.liveChatting.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "live_chatting")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveChatting {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long liveChattingId;
	private Long liveBroadcastId;
	private Long memberId;
	private String content;
	private LocalDateTime time;

}
