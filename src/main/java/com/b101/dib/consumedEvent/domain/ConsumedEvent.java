package com.b101.dib.consumedEvent.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Consumer 가 처리한 (group, eventId). 처리 트랜잭션과 같이 커밋되어 재전달을 걸러낸다
@Entity
@Table(name = "consumed_event")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumedEvent {
    @EmbeddedId
    private ConsumedEventId id;

    private LocalDateTime consumedAt;

    public static ConsumedEvent of(String consumerGroup, String eventId) {
        return new ConsumedEvent(new ConsumedEventId(consumerGroup, eventId), LocalDateTime.now());
    }
}
