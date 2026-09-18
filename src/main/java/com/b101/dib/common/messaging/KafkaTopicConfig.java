package com.b101.dib.common.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// 서버 기동 시 KafkaAdmin 이 없는 토픽을 만든다 (브로커가 없으면 경고만 남기고 기동은 계속)
@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic bidPlacedTopic() {
        return TopicBuilder.name(KafkaTopics.BID_PLACED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auctionClosedTopic() {
        return TopicBuilder.name(KafkaTopics.AUCTION_CLOSED).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic recommendationRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.RECOMMENDATION_REQUESTED).partitions(3).replicas(1).build();
    }
}
