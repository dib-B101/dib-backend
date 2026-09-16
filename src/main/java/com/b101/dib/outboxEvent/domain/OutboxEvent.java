package com.b101.dib.outboxEvent.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    private static final int ERROR_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxEventId;

    private String eventId;
    private String aggregateType;
    private Long aggregateId;
    private String eventType;
    private String topic;

    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    private LocalDateTime occurredAt;
    private LocalDateTime publishedAt;
    private Integer attempts;
    private String lastError;

    public static OutboxEvent of(String aggregateType, Long aggregateId, String eventType, String topic, String payloadJson) {
        return OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .topic(topic)
                .payload(payloadJson)
                .occurredAt(LocalDateTime.now())
                .attempts(0)
                .build();
    }

    public void markPublished() {
        publishedAt = LocalDateTime.now();
        attempts = attempts + 1;
        lastError = null;
    }

    public void markFailed(String error) {
        attempts = attempts + 1;
        lastError = error == null ? null : (error.length() > ERROR_MAX ? error.substring(0, ERROR_MAX) : error);
    }
}
