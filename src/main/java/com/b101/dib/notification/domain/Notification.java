package com.b101.dib.notification.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    private static final int CONTENT_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long auctionId;
    private Long memberId;
    private Long liveBroadcastId;
    private Long productId;
    private Long bidId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private NotificationType type;

    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static Notification paymentFailed(Long buyerId, Long orderId, String reason) {
        String content = "주문 #" + orderId + " 결제에 실패했습니다: " + reason + ". 결제 기한 안에 카드를 확인해 주세요.";
        if (content.length() > CONTENT_MAX) {
            content = content.substring(0, CONTENT_MAX);
        }
        return Notification.builder()
                .memberId(buyerId)
                .type(NotificationType.SYSTEM)
                .title("결제 실패")
                .content(content)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
