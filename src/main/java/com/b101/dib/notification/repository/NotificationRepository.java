package com.b101.dib.notification.repository;

import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findTopByAuctionIdAndMemberIdAndTypeAndTitleOrderByCreatedAtDesc(
            Long auctionId, Long memberId, NotificationType type, String title);

    List<Notification> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}
