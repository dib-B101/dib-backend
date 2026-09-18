package com.b101.dib.common.messaging;

// Kafka 토픽 이름. dib.<aggregate>.<event>. 메시지 키는 aggregateId (같은 경매 이벤트는 한 파티션에서 순서 보장)
public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String BID_PLACED = "dib.bid.placed";
    public static final String AUCTION_CLOSED = "dib.auction.closed";
    public static final String RECOMMENDATION_REQUESTED = "dib.recommendation.requested";

    // Consumer group. 역할별로 나눠서 한 역할이 느려도 다른 역할이 밀리지 않게
    public static final String GROUP_NOTIFICATION = "dib-notification";
    public static final String GROUP_MEMBER_EVENT = "dib-member-event";
    public static final String GROUP_PAYMENT = "dib-payment";
    public static final String GROUP_FRAUD = "dib-fraud";
    public static final String GROUP_RECOMMENDATION = "dib-recommendation";
}
