-- Outbox: 비즈니스 데이터와 같은 트랜잭션에 이벤트를 남기고, OutboxPublisher 가 커밋된 행을 Kafka 로 발행한다
CREATE TABLE outbox_event (
    outbox_event_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_outbox_event PRIMARY KEY (outbox_event_id),
    event_id VARCHAR(36) NOT NULL, CONSTRAINT uq_outbox_event_event_id UNIQUE (event_id),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_error VARCHAR(500)
);
CREATE INDEX ix_outbox_event_unpublished ON outbox_event (outbox_event_id) WHERE published_at IS NULL;

-- Consumer 멱등성: (consumer group, event_id) 를 처리 트랜잭션 안에서 함께 저장해 같은 이벤트를 두 번 처리하지 않는다
CREATE TABLE consumed_event (
    consumer_group VARCHAR(50) NOT NULL,
    event_id VARCHAR(36) NOT NULL,
    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_consumed_event PRIMARY KEY (consumer_group, event_id)
);

-- AI 이상입찰 콜백(명세 93)은 피처 일부를 null 로 준다 (auction_duration 은 항상 null, ml_score 는 모델 미가동 시 null)
ALTER TABLE fraud_detection ALTER COLUMN bidder_tendency DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN bidding_ratio DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN last_bidding DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN auction_bids DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN starting_price_average DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN early_bidding DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN winning_ratio DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN auction_duration DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN rule_score DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN ml_score DROP NOT NULL;
ALTER TABLE fraud_detection ALTER COLUMN model_version DROP NOT NULL;
