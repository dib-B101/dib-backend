-- ERD(dib.sql) 기준 정합성 반영
--
-- V2의 PostgreSQL 번역 규칙을 그대로 유지한다:
--   ERD의 VARCHAR + COMMENT 'ENUM(...)' 표기 -> 네이티브 ENUM 타입
--   ERD의 TINYINT -> SMALLINT, DOUBLE -> DOUBLE PRECISION (PostgreSQL에 없는 타입)
--   PK -> GENERATED ALWAYS AS IDENTITY, 금액 -> BIGINT
--   ERD에 없는 FK 제약은 V2 방식대로 명시한다
--
-- ERD를 그대로 따르지 않은 지점은 각 항목에 이유를 남겼다.


-- ─────────────────────────────────────────────
-- 1. member
-- ─────────────────────────────────────────────
-- ERD는 suspended_until (정지 만료 시각). V2가 suspended_at 으로 잘못 옮겼다.
ALTER TABLE member RENAME COLUMN suspended_at TO suspended_until;

-- ERD: password NOT NULL ('암호화 저장')
ALTER TABLE member ALTER COLUMN password SET NOT NULL;


-- ─────────────────────────────────────────────
-- 2. auction
-- ─────────────────────────────────────────────
-- V2가 AUCTION_STATUS 타입을 만들어놓고 컬럼은 VARCHAR(20)으로 뒀다. 타입을 실제로 적용한다.
ALTER TABLE auction
    ALTER COLUMN status TYPE AUCTION_STATUS USING status::AUCTION_STATUS;

-- bid_unit_policy 제거 (ERD에 없음).
-- V2가 참조한 'docs/Initial Schema.sql'에서 넘어온 컬럼이지만 그 원본은 저장소에 없고,
-- 호가 단위를 구간별로 차등하는 요구사항도 문서·코드 어디에도 없다.
-- 입찰 단위는 bid_unit 하나로 표현한다. 구간별 차등이 다시 필요해지면 그때 재설계한다.
ALTER TABLE auction DROP COLUMN bid_unit_policy;


-- ─────────────────────────────────────────────
-- 3. order
-- ─────────────────────────────────────────────
-- ERD: PENDING_PAYMENT (V2는 PENDING). 라벨만 바꾸므로 기존 행 데이터도 함께 따라간다.
ALTER TYPE ORDER_STATUS RENAME VALUE 'PENDING' TO 'PENDING_PAYMENT';


-- ─────────────────────────────────────────────
-- 4. payment
-- ─────────────────────────────────────────────
-- ERD 주석은 ENUM('CARD', 'TRANSFER') 인데 V2가 VARCHAR(255)로 뒀다.
-- auction.status 와 같은 케이스 — 네이티브 ENUM 타입으로 맞춘다.
CREATE TYPE PAYMENT_TYPE AS ENUM('CARD', 'TRANSFER');

ALTER TABLE payment
    ALTER COLUMN type TYPE PAYMENT_TYPE USING type::PAYMENT_TYPE;


-- ─────────────────────────────────────────────
-- 5. member_event
-- ─────────────────────────────────────────────
-- ERD: auction/product/category 참조가 NULL 허용.
-- SEARCH, IMPRESSION 처럼 특정 경매와 무관한 이벤트가 있으므로 ERD가 맞다.
ALTER TABLE member_event ALTER COLUMN auction_id  DROP NOT NULL;
ALTER TABLE member_event ALTER COLUMN product_id  DROP NOT NULL;
ALTER TABLE member_event ALTER COLUMN category_id DROP NOT NULL;


-- ─────────────────────────────────────────────
-- 6. fraud_detection
-- ─────────────────────────────────────────────
-- ERD: 규칙 기반 점수와 학습 모델 기반 점수를 분리해 추가.
-- 기존 더미 행이 있어 DEFAULT 0 이 필요하다.
ALTER TABLE fraud_detection
    ADD COLUMN rule_score DOUBLE PRECISION NOT NULL DEFAULT 0,
    ADD COLUMN ml_score   DOUBLE PRECISION NOT NULL DEFAULT 0;


-- ─────────────────────────────────────────────
-- 7. 라이브 방송
-- ─────────────────────────────────────────────
CREATE TYPE LIVE_BROADCAST_STATUS AS ENUM('SCHEDULED', 'LIVE', 'ENDED', 'CANCELLED');

CREATE TABLE live_broadcast (
    live_broadcast_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_live_broadcast PRIMARY KEY (live_broadcast_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_live_broadcast_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status LIVE_BROADCAST_STATUS NOT NULL DEFAULT 'SCHEDULED',
    stream_url VARCHAR(500),
    scheduled_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    view_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TYPE LIVE_AUCTION_STATUS AS ENUM('WAITING', 'PROCEEDING', 'COMPLETED');

CREATE TABLE live_auction (
    live_auction_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_live_auction PRIMARY KEY (live_auction_id),
    live_broadcast_id BIGINT NOT NULL, CONSTRAINT fk_live_auction_broadcast FOREIGN KEY (live_broadcast_id) REFERENCES live_broadcast (live_broadcast_id),
    auction_id BIGINT NOT NULL, CONSTRAINT fk_live_auction_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    show_sequence INTEGER NOT NULL,
    status LIVE_AUCTION_STATUS NOT NULL DEFAULT 'WAITING'
);


-- ─────────────────────────────────────────────
-- 8. 구독
-- ─────────────────────────────────────────────
CREATE TABLE subscription (
    subscription_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_subscription PRIMARY KEY (subscription_id),
    broadcaster_id BIGINT NOT NULL, CONSTRAINT fk_subscription_broadcaster FOREIGN KEY (broadcaster_id) REFERENCES member (member_id),
    subscriber_id BIGINT NOT NULL, CONSTRAINT fk_subscription_subscriber FOREIGN KEY (subscriber_id) REFERENCES member (member_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ─────────────────────────────────────────────
-- 9. 알림
-- ─────────────────────────────────────────────
CREATE TYPE NOTIFICATION_TYPE AS ENUM('LIVE_STARTED', 'OUTBID', 'BOOKMARK_STARTED', 'AUCTION_WON', 'SYSTEM');
CREATE TYPE NOTIFICATION_CHANNEL AS ENUM('BID', 'AUCTION', 'TRADE', 'SHIPPING', 'DEADLINE');
CREATE TYPE NOTIFICATION_STATUS AS ENUM('PENDING', 'SENT', 'FAILED');

-- ERD는 auction_id/live_broadcast_id/product_id/bid_id 를 모두 NOT NULL 로 뒀는데,
-- type 에 SYSTEM 이 있어 그대로 두면 시스템 알림을 저장할 수 없다.
-- 수신자(member_id)만 NOT NULL 로 두고 나머지 참조는 NULL 허용으로 완화했다.
CREATE TABLE notification (
    notification_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_notification PRIMARY KEY (notification_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_notification_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    auction_id BIGINT, CONSTRAINT fk_notification_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    live_broadcast_id BIGINT, CONSTRAINT fk_notification_broadcast FOREIGN KEY (live_broadcast_id) REFERENCES live_broadcast (live_broadcast_id),
    product_id BIGINT, CONSTRAINT fk_notification_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    bid_id BIGINT, CONSTRAINT fk_notification_bid FOREIGN KEY (bid_id) REFERENCES bid (bid_id),
    type NOTIFICATION_TYPE,
    title VARCHAR(30),
    content VARCHAR(500) NOT NULL,
    channel NOTIFICATION_CHANNEL NOT NULL,
    related_url VARCHAR(500),
    dedup_key VARCHAR(255),
    status NOTIFICATION_STATUS,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    failed_at TIMESTAMP,
    failure_reason VARCHAR(255)
);


-- ─────────────────────────────────────────────
-- 10. Outbox (Kafka 발행 보장)
-- ─────────────────────────────────────────────
-- ERD 원문의 이 테이블은 COMMENT 따옴표가 깨져 status/retry_count 정의가 유실됐다.
-- status 는 주석에 남은 PENDING/PUBLISHED/FAILED 로, retry_count 는 INTEGER DEFAULT 0 으로 복원했다.
CREATE TYPE OUTBOX_EVENT_STATUS AS ENUM('PENDING', 'PUBLISHED', 'FAILED');

CREATE TABLE outbox_event (
    outbox_event_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_outbox_event PRIMARY KEY (outbox_event_id),
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    partition_key VARCHAR(100),
    payload JSONB NOT NULL,
    status OUTBOX_EVENT_STATUS NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0
);
