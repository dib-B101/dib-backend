package com.b101.dib.consumedEvent.repository;

import com.b101.dib.consumedEvent.domain.ConsumedEvent;
import com.b101.dib.consumedEvent.domain.ConsumedEventId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, ConsumedEventId> {
}
