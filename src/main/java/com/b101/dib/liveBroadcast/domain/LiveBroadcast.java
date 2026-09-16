package com.b101.dib.liveBroadcast.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "live_broadcast")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveBroadcast {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long liveBroadcastId;
	
	Long memberId;
	String title;
	String description;
	
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
	LiveBroadcastStatus status;
    
	String livekitRoomName;
	LocalDateTime startedAt;
	LocalDateTime endedAt;
	Integer viewCount;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;

}
