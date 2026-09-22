package com.b101.dib.common.messaging;

// Kafka 토픽 이름. dib.<aggregate>.<event>. 메시지 키는 aggregateId (같은 경매 이벤트는 한 파티션에서 순서 보장)
public final class KafkaTopics {
    private KafkaTopics() {}

    public static final String BID_PLACED = "dib.bid.placed";
    public static final String AUCTION_CLOSED = "dib.auction.closed";
    public static final String AUCTION_STARTED = "dib.auction.started";
    public static final String LIVE_STARTED = "dib.live.started";
    public static final String ORDER_CONFIRMED = "dib.order.confirmed";
    public static final String RECOMMENDATION_REQUESTED = "dib.recommendation.requested";

    // Consumer group. 역할별로 나눠서 한 역할이 느려도 다른 역할이 밀리지 않게
    public static final String GROUP_NOTIFICATION = "dib-notification";
    // 찜한 사람 팬아웃은 한 건이 수백 명으로 불어날 수 있어 알림 그룹과 분리한다
    public static final String GROUP_BOOKMARK = "dib-bookmark";
    public static final String GROUP_REVIEW = "dib-review";
    public static final String GROUP_MEMBER_EVENT = "dib-member-event";
    public static final String GROUP_PAYMENT = "dib-payment";
    public static final String GROUP_FRAUD = "dib-fraud";
    public static final String GROUP_RECOMMENDATION = "dib-recommendation";
}
