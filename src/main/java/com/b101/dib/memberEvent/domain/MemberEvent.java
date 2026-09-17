package com.b101.dib.memberEvent.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

// 사용자 행동 로그 (추천·이상탐지 입력). 요청 트랜잭션이 아니라 Kafka Consumer 가 적재한다
@Entity
@Table(name = "member_event")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberEventId;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private MemberEventType eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    private String metadata;

    @Column(name = "occured_at")   // 스키마 컬럼명 오타 그대로
    private LocalDateTime occuredAt;

    private Long auctionId;
    private Long productId;
    private Long categoryId;

    public static MemberEvent bid(Long memberId, Long auctionId, Long productId, Long categoryId, String metadataJson, LocalDateTime occuredAt) {
        return MemberEvent.builder()
                .memberId(memberId)
                .eventType(MemberEventType.BID)
                .metadata(metadataJson)
                .occuredAt(occuredAt == null ? LocalDateTime.now() : occuredAt)
                .auctionId(auctionId)
                .productId(productId)
                .categoryId(categoryId)
                .build();
    }
}
