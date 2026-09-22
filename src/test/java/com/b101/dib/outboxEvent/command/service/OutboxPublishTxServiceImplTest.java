package com.b101.dib.outboxEvent.command.service;

import com.b101.dib.outboxEvent.domain.OutboxEvent;
import com.b101.dib.outboxEvent.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxPublishTxServiceImplTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final int RETENTION_DAYS = 7;

    @Mock OutboxEventRepository outboxEventRepository;
    @Mock KafkaTemplate<String, Object> kafkaTemplate;

    private OutboxPublishTxServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OutboxPublishTxServiceImpl(outboxEventRepository, kafkaTemplate, new ObjectMapper());
        ReflectionTestUtils.setField(service, "maxAttempts", MAX_ATTEMPTS);
        ReflectionTestUtils.setField(service, "retentionDays", RETENTION_DAYS);
    }

    private static OutboxEvent event() {
        return OutboxEvent.of("AUCTION", 17L, "AUCTION_CLOSED", "dib.auction.closed", "{\"auctionId\":17}");
    }

    @Test
    void marksPublishedWhenBrokerAcks() {
        OutboxEvent event = event();
        given(outboxEventRepository.lockUnpublished(anyInt(), anyInt())).willReturn(List.of(event));
        given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.completedFuture(null));

        assertThat(service.publishBatch(100)).isEqualTo(1);
        assertThat(event.getPublishedAt()).isNotNull();
    }

    // 상한을 안 넘기면 행이 그대로 남아 다음 폴링에서 다시 시도된다 — 유실 없음
    @Test
    void keepsRowUnpublishedWhenSendFails() {
        OutboxEvent event = event();
        given(outboxEventRepository.lockUnpublished(anyInt(), anyInt())).willReturn(List.of(event));
        given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));

        assertThat(service.publishBatch(100)).isZero();
        assertThat(event.getPublishedAt()).isNull();
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getLastError()).contains("broker down");
    }

    // 상한이 없으면 영구 실패 행이 제일 오래된 행이라 매 폴링마다 먼저 집혀 뒤의 정상 이벤트를 막는다
    @Test
    void asksRepositoryToSkipRowsOverAttemptLimit() {
        given(outboxEventRepository.lockUnpublished(anyInt(), anyInt())).willReturn(List.of());

        service.publishBatch(50);

        verify(outboxEventRepository).lockUnpublished(50, MAX_ATTEMPTS);
    }

    @Test
    void countsAbandonedRowsAgainstTheSameLimit() {
        given(outboxEventRepository.countAbandoned(MAX_ATTEMPTS)).willReturn(2L);

        assertThat(service.countAbandoned()).isEqualTo(2L);
    }

    @Test
    void purgesPublishedRowsOlderThanRetention() {
        given(outboxEventRepository.deletePublishedBefore(any())).willReturn(4);

        assertThat(service.purgePublished()).isEqualTo(4);

        ArgumentCaptor<LocalDateTime> cutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(outboxEventRepository).deletePublishedBefore(cutoff.capture());
        assertThat(cutoff.getValue())
                .isBefore(LocalDateTime.now().minusDays(RETENTION_DAYS).plusMinutes(1))
                .isAfter(LocalDateTime.now().minusDays(RETENTION_DAYS).minusMinutes(1));
    }

    @Test
    void usesAggregateIdAsMessageKeySoOneAuctionStaysOrdered() {
        OutboxEvent event = event();
        given(outboxEventRepository.lockUnpublished(anyInt(), anyInt())).willReturn(List.of(event));
        given(kafkaTemplate.send(anyString(), anyString(), any()))
                .willReturn(CompletableFuture.completedFuture(null));

        service.publishBatch(100);

        verify(kafkaTemplate).send(eq("dib.auction.closed"), eq("17"), any());
    }
}
