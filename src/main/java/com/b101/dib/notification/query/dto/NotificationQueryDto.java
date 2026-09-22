package com.b101.dib.notification.query.dto;

import com.b101.dib.notification.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NotificationQueryDto {
    private Long notificationId;
    private NotificationType type;
    private String title;
    private String content;
    @JsonProperty("isRead")
    private boolean read;
    private Long auctionId;
    private Long productId;
    private Long liveBroadcastId;
    private Long bidId;
    private Long orderId;    // 평가 요청(REVIEW_REQUEST) 알림이 든다. 없으면 앱이 어느 거래인지 못 찾아 탭해도 안 열린다
    private LocalDateTime createdAt;
}
