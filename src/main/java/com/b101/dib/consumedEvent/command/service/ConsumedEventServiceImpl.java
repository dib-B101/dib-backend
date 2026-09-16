package com.b101.dib.consumedEvent.command.service;

import com.b101.dib.consumedEvent.domain.ConsumedEvent;
import com.b101.dib.consumedEvent.domain.ConsumedEventId;
import com.b101.dib.consumedEvent.repository.ConsumedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsumedEventServiceImpl implements ConsumedEventService {
    private final ConsumedEventRepository consumedEventRepository;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean claim(String consumerGroup, String eventId) {
        if (consumedEventRepository.existsById(new ConsumedEventId(consumerGroup, eventId))) {
            return false;   // 같은 그룹은 파티션을 순서대로 읽으므로 exists → insert 사이 경쟁은 없다
        }
        consumedEventRepository.save(ConsumedEvent.of(consumerGroup, eventId));
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claimInNewTransaction(String consumerGroup, String eventId) {
        return claim(consumerGroup, eventId);
    }
}
