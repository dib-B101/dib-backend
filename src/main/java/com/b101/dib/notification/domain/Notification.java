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
    public static final String OFFER_TITLE = "차순위 낙찰 안내";
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
        return system(buyerId, "결제 실패",
                "주문 #" + orderId + " 결제에 실패했습니다: " + reason + ". 결제 기한 안에 카드를 확인해 주세요.");
    }

    public static Notification system(Long memberId, String title, String content) {
        return Notification.builder()
                .memberId(memberId)
                .type(NotificationType.SYSTEM)
                .title(title)
                .content(cut(content))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Notification offer(Long auctionId, Long memberId, Long amount) {
        return Notification.builder()
                .auctionId(auctionId)
                .memberId(memberId)
                .type(NotificationType.AUCTION_WON)
                .title(OFFER_TITLE)
                .content(cut("낙찰자가 결제하지 않아 차순위로 낙찰 기회가 넘어왔습니다. 입찰가 " + amount
                        + "원으로 24시간 안에 수락하면 주문이 생성됩니다."))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Notification won(Long auctionId, Long memberId, Long bidId, Long amount) {
        return Notification.builder()
                .auctionId(auctionId)
                .memberId(memberId)
                .bidId(bidId)
                .type(NotificationType.AUCTION_WON)
                .title("낙찰 안내")
                .content(cut(amount + "원에 낙찰되었습니다. 등록된 카드로 자동 결제가 진행됩니다."))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Notification outbid(Long auctionId, Long memberId, Long bidId, Long newPrice) {
        return Notification.builder()
                .auctionId(auctionId)
                .memberId(memberId)
                .bidId(bidId)
                .type(NotificationType.OUTBID)
                .title("상위 입찰 발생")
                .content(cut("다른 참가자가 " + newPrice + "원으로 입찰했습니다. 다시 입찰해 보세요."))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static String cut(String s) {
        return s.length() > CONTENT_MAX ? s.substring(0, CONTENT_MAX) : s;
    }
}
