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
    private Long orderId;
    private LocalDateTime createdAt;
}
