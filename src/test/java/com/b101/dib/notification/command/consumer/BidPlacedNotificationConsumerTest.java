package com.b101.dib.notification.command.consumer;

import com.b101.dib.common.messaging.KafkaTopics;
import com.b101.dib.consumedEvent.command.service.ConsumedEventService;
import com.b101.dib.notification.domain.Notification;
import com.b101.dib.notification.domain.NotificationType;
import com.b101.dib.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BidPlacedNotificationConsumerTest {

    @Mock NotificationRepository notificationRepository;
    @Mock ConsumedEventService consumedEventService;

    private BidPlacedNotificationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new BidPlacedNotificationConsumer(
                notificationRepository, consumedEventService, new ObjectMapper());
    }

    @Test
    void higherBidNotifiesPreviousTopBidder() {
        given(consumedEventService.claim(KafkaTopics.GROUP_NOTIFICATION, "event-1")).willReturn(true);

        consumer.onBidPlaced("""
                {
                  "eventType":"BID_PLACED",
                  "eventId":"event-1",
                  "aggregateType":"AUCTION",
                  "aggregateId":7,
                  "payload":{
                    "auctionId":7,
                    "bidId":31,
                    "amount":12000,
                    "previousTopBidderId":22
                  }
                }
                """);

        ArgumentCaptor<Notification> saved = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(saved.capture());
        assertThat(saved.getValue().getMemberId()).isEqualTo(22L);
        assertThat(saved.getValue().getAuctionId()).isEqualTo(7L);
        assertThat(saved.getValue().getBidId()).isEqualTo(31L);
        assertThat(saved.getValue().getType()).isEqualTo(NotificationType.OUTBID);
    }

    @Test
    void duplicateEventDoesNotCreateNotification() {
        given(consumedEventService.claim(KafkaTopics.GROUP_NOTIFICATION, "event-1")).willReturn(false);

        consumer.onBidPlaced("""
                {"eventId":"event-1","payload":{"previousTopBidderId":22}}
                """);

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
