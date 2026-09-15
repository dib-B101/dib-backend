package com.b101.dib.auth.command.event;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PasswordChangedEventListener {

    private static final String TOPIC = "member.password-changed.v1";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(PasswordChangedEvent event) {
        kafkaTemplate.send(TOPIC, event.memberId().toString(), event);
    }
}
