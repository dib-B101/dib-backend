package com.b101.dib.notification.domain;

import jakarta.persistence.PostPersist;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// 알림 행이 저장되면(어느 서비스에서든) 커밋 후 NotificationCreatedEvent 를 낸다 → websocket 이 받아 푸시.
// 저장하는 쪽(입찰·종료·만료·결제 등)은 손대지 않는다. Spring Boot 가 JPA 리스너에 빈 주입을 지원
@Component
public class NotificationPushListener {
    private final ApplicationEventPublisher eventPublisher;

    public NotificationPushListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostPersist
    public void afterSave(Notification notification) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            eventPublisher.publishEvent(new NotificationCreatedEvent(notification));
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new NotificationCreatedEvent(notification));
            }
        });
    }
}
