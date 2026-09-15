package com.b101.dib.notification.command.service;

public interface NotificationCommandService {
    void read(Long memberId, Long notificationId);
    int readAll(Long memberId);
}
