package com.b101.dib.auth.command.event;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordChangedEventListenerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void publishesPasswordChangedEventWithMemberIdKey() {
        PasswordChangedEvent event = new PasswordChangedEvent(
                "event-id",
                1L,
                Instant.parse("2026-09-15T03:00:00Z")
        );
        PasswordChangedEventListener listener =
                new PasswordChangedEventListener(kafkaTemplate);

        listener.publish(event);

        verify(kafkaTemplate).send("member.password-changed.v1", "1", event);
    }
}
