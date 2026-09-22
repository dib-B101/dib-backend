package com.b101.dib.notification.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {
    @Test
    void refundNotificationTargetsOrder() {
        Notification notification = Notification.order(7L, 31L, "환불 완료", "환불되었습니다.");

        assertThat(notification.resourceType()).isEqualTo("ORDER");
        assertThat(notification.resourceId()).isEqualTo(31L);
        assertThat(notification.getMemberId()).isEqualTo(7L);
    }
}
