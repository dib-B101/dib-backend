package com.b101.dib.notification.domain;

import jakarta.persistence.*;
import com.b101.dib.product.domain.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@EntityListeners(NotificationPushListener.class)
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
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private NotificationType type;

    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static Notification paymentFailed(Long buyerId, Long orderId, String reason) {
        return order(orderId, buyerId, "결제 실패",
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

    public static Notification productModerated(Long productId, Long memberId, String productTitle, ProductStatus status) {
        boolean approved = status == ProductStatus.REGISTERED;
        return Notification.builder()
                .productId(productId)
                .memberId(memberId)
                .type(NotificationType.SYSTEM)
                .title(approved ? "상품 검수 승인" : "상품 등록 거절")
                .content(cut("‘" + productTitle + "’ " + (approved
                        ? "상품의 검수가 완료됐습니다. 등록 상품 관리에서 경매를 시작할 수 있어요."
                        : "상품 등록이 거절됐습니다. 등록 상품 관리에서 사유를 확인하고 수정해주세요.")))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 주문에 딸린 안내(결제·배송·취소 등). orderId 를 실어야 앱이 탭했을 때 그 거래 화면을 연다.
    // 앱은 이 알림을 구매자 화면으로 열므로 구매자에게 가는 알림에만 쓴다. 판매자용(정산·보류)은 system() 그대로
    public static Notification order(Long orderId, Long memberId, String title, String content) {
        return Notification.builder()
                .memberId(memberId)
                .orderId(orderId)
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

    // 찜한 상품의 경매가 시작됐을 때. 찜을 누르는 이유가 "시작하면 알려줘" 인데 그동안 이 알림이 없었다
    public static Notification bookmarkStarted(Long auctionId, Long productId, Long memberId, String productTitle) {
        return Notification.builder()
                .auctionId(auctionId)
                .productId(productId)
                .memberId(memberId)
                .type(NotificationType.BOOKMARK_STARTED)
                .title("찜한 상품 경매 시작")
                .content(cut("‘" + productTitle + "’ 경매가 시작됐습니다. 지금 입찰할 수 있어요."))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 찜한 상품이 편성된 라이브 방송이 켜졌을 때. 판매자 팔로우가 없어서 "찜" 을 관심 신호로 쓴다
    public static Notification liveStarted(Long liveBroadcastId, Long productId, Long memberId, String productTitle) {
        return Notification.builder()
                .liveBroadcastId(liveBroadcastId)
                .productId(productId)
                .memberId(memberId)
                .type(NotificationType.LIVE_STARTED)
                .title("찜한 상품 라이브 시작")
                .content(cut("‘" + productTitle + "’ 이(가) 나온 라이브 방송이 시작됐습니다."))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // 구매확정 직후 구매자에게. 여기서 받은 별점이 판매자 평점이 된다
    public static Notification reviewRequest(Long orderId, Long memberId, String productTitle) {
        return Notification.builder()
                .memberId(memberId)
                .orderId(orderId)
                .type(NotificationType.REVIEW_REQUEST)
                .title("판매자는 어떠셨나요?")
                .content(cut("‘" + productTitle + "’ 거래가 끝났습니다. 별점으로 평가해 주세요. (주문 #" + orderId + ")"))
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markRead() {
        isRead = true;
    }

    // 프론트 DomainNotification 의 resourceType / resourceId
    public String resourceType() {
        if (orderId != null) {
            return "ORDER";
        }
        if (liveBroadcastId != null) {
            return "LIVE";
        }
        if (auctionId != null) {
            return "AUCTION";
        }
        if (productId != null) {
            return "PRODUCT";
        }
        return "SYSTEM";
    }

    public Long resourceId() {
        if (orderId != null) {
            return orderId;
        }
        if (liveBroadcastId != null) {
            return liveBroadcastId;
        }
        if (auctionId != null) {
            return auctionId;
        }
        if (productId != null) {
            return productId;
        }
        return notificationId;
    }

    private static String cut(String s) {
        return s.length() > CONTENT_MAX ? s.substring(0, CONTENT_MAX) : s;
    }
}
