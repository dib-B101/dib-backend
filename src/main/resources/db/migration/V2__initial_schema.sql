-- 팀 초기 스키마 (dib-orchestration/docs/Initial Schema.sql 기준)
-- Flyway가 관리하므로 원본의 DROP/CREATE SCHEMA, CREATE EXTENSION(V1에서 처리)은 제외


-- VECTOR 타입을 사용하기 위한 확장 모듈 (필요시 주석 해제)
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TYPE GENDER AS ENUM('MALE', 'FEMALE');
CREATE TYPE MEMBER_STATUS AS ENUM('ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'EXPELLED');
CREATE TYPE MEMBER_ROLE AS ENUM('USER', 'ADMIN');
CREATE TABLE member (
    member_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_member PRIMARY KEY (member_id),
    email VARCHAR(255) NOT NULL, CONSTRAINT up_member_email UNIQUE (email),
    password VARCHAR(255),
    nickname VARCHAR(50) NOT NULL, CONSTRAINT uq_member_nickname UNIQUE (nickname),
    name VARCHAR(10) NOT NULL,
    gender GENDER NOT NULL,
    birth_date DATE NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    status MEMBER_STATUS NOT NULL DEFAULT 'ACTIVE',
    role MEMBER_ROLE NOT NULL DEFAULT 'USER',
    score DOUBLE PRECISION NOT NULL DEFAULT 50,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
	suspended_at TIMESTAMP,
    bank_name VARCHAR(100),
    account_holder VARCHAR(50),
    account_number VARCHAR(100),
	warning_count INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE category (
    category_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_category PRIMARY KEY (category_id),
    name VARCHAR(100) NOT NULL, CONSTRAINT uq_category_name UNIQUE (name)
);

CREATE TYPE PRODUCT_CONDITION AS ENUM('GOOD', 'NORMAL', 'BAD');
CREATE TYPE PRODUCT_STATUS AS ENUM('PENDING', 'REGISTERED', 'REJECTED', 'SOLD', 'CANCELED');
CREATE TABLE product (
    product_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_product PRIMARY KEY (product_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_product_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    category_id BIGINT NOT NULL, CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (category_id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    condition PRODUCT_CONDITION NOT NULL,
    model_name VARCHAR(200),
    release_year INTEGER,
    market_price BIGINT,
    thumbnail_url VARCHAR(500),
    status PRODUCT_STATUS NOT NULL DEFAULT 'PENDING',
    embedding vector(768),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE TABLE product_image (
    product_image_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_product_image PRIMARY KEY (product_image_id),
    product_id BIGINT NOT NULL, CONSTRAINT fk_product_image_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    image_url VARCHAR(500) NOT NULL,
    sequence INTEGER NOT NULL
);

CREATE TABLE address (
    address_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_address PRIMARY KEY (address_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_address_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    number VARCHAR(50),
    address VARCHAR(500),
    name VARCHAR(100) NOT NULL,
    api_address_id VARCHAR(500) NOT NULL
);

CREATE TYPE LIVE_STATUS AS ENUM('SCHEDULED', 'LIVE', 'ENDED', 'CANCELED');
CREATE TABLE live_broadcast (
    live_broadcast_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_live_broadcast PRIMARY KEY (live_broadcast_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_live_broadcast_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status LIVE_STATUS NOT NULL DEFAULT 'SCHEDULED',
    stream_url VARCHAR(500),                     -- 방송 스트리밍 URL (또는 Stream Key)
    scheduled_at TIMESTAMP NOT NULL,             -- 방송 예정 시간
    started_at TIMESTAMP,                        -- 실제 방송 시작 시간
    ended_at TIMESTAMP,                          -- 방송 종료 시간
    view_count INTEGER NOT NULL DEFAULT 0,       -- 누적 시청자 수
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE live_chatting (
	live_chatting_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_live_chatting PRIMARY KEY (live_chatting_id),
	live_broadcast_id BIGINT NOT NULL, CONSTRAINT fk_live_chatting_live_broadcast FOREIGN KEY (live_broadcast_id) REFERENCES live_broadcast (live_broadcast_id),
	member_id BIGINT NOT NULL, CONSTRAINT fk_live_chatting_member FOREIGN KEY (member_id) REFERENCES member (member_id),
	content TEXT NOT NULL,
	time TIMESTAMP NOT NULL
);

CREATE TYPE AUCTION_STATUS AS ENUM('SCHEDULED', 'DEPOSITED', 'ACTIVE', 'ENDED', 'REFUNDED', 'CANCELED');
CREATE TABLE auction (
    auction_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_auction PRIMARY KEY (auction_id),
    product_id BIGINT NOT NULL, CONSTRAINT fk_auction_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    start_price BIGINT NOT NULL,
    current_price BIGINT NOT NULL,
	auction_time INTEGER NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    status AUCTION_STATUS NOT NULL DEFAULT 'SCHEDULED',
    bid_count INTEGER NOT NULL DEFAULT 0,
    bidder_count INTEGER NOT NULL DEFAULT 0,
    view_count INTEGER NOT NULL DEFAULT 0,
    bookmark_count INTEGER NOT NULL DEFAULT 0,
    top_bid_id BIGINT,
    extension_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
	live_broadcast_id BIGINT NULL, CONSTRAINT fk_auction_live_broadcast FOREIGN KEY (live_broadcast_id) REFERENCES live_broadcast(live_broadcast_id)
);

CREATE TABLE bid (
    bid_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_bid PRIMARY KEY (bid_id),
    auction_id BIGINT NOT NULL, CONSTRAINT fk_bid_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_bid_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    amount BIGINT NOT NULL, CONSTRAINT uq_bid_auction_amount UNIQUE (auction_id, amount),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE auction 
ADD CONSTRAINT fk_auction_top_bid FOREIGN KEY (top_bid_id) REFERENCES bid (bid_id);

CREATE TABLE bookmark (
	bookmark_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_bookmark PRIMARY KEY (bookmark_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_bookmark_member FOREIGN KEY (member_id) REFERENCES member (member_id),
	auction_id BIGINT NOT NULL, CONSTRAINT fk_bookmark_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id)
);

CREATE TYPE BID_DEPOSIT_STATUS AS ENUM('PENDING', 'PAID', 'REFUNDED');
CREATE TABLE bid_deposit (
    bid_deposit_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_bid_deposit PRIMARY KEY (bid_deposit_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_bid_deposit_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    auction_id BIGINT NOT NULL, CONSTRAINT fk_bid_deposit_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    amount BIGINT NOT NULL,
    status BID_DEPOSIT_STATUS NOT NULL DEFAULT 'PENDING',
    released_at TIMESTAMP
);

CREATE TYPE ORDER_STATUS AS ENUM('PENDING', 'PAID', 'PREPARING', 'SHIPPED', 'DELIEVERED', 'CONFIRMED', 'CANCELED', 'REFUNDED');
CREATE TABLE "order" (
    order_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_order PRIMARY KEY (order_id),
    auction_id BIGINT NOT NULL, CONSTRAINT fk_order_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    seller_id BIGINT NOT NULL, CONSTRAINT fk_order_seller FOREIGN KEY (seller_id) REFERENCES member (member_id),
    buyer_id BIGINT NOT NULL, CONSTRAINT fk_order_buyer FOREIGN KEY (buyer_id) REFERENCES member (member_id),
    final_price BIGINT NOT NULL,
    status ORDER_STATUS NOT NULL,
    payment_due TIMESTAMP NOT NULL,
    address JSONB,
    tracking_number VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    chatting_session_id VARCHAR(100) NOT NULL
);

CREATE TYPE PAYMENT_TYPE AS ENUM('CARD', 'TRANSFER');
CREATE TABLE payment (
	payment_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_payment PRIMARY KEY (payment_id),
	order_id BIGINT NOT NULL, CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES "order" (order_id),
	buyer_id BIGINT NOT NULL, CONSTRAINT fk_payment_buyer FOREIGN KEY (buyer_id) REFERENCES member (member_id),
	amount BIGINT NOT NULL,
	type PAYMENT_TYPE NOT NULL,
	refund_key VARCHAR(500)		NULL,
	receipt_url VARCHAR(500)		NULL,
	paid_at TIMESTAMP	DEFAULT CURRENT_TIMESTAMP	NOT NULL
);

CREATE TABLE settlement (
	settlement_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_settlement PRIMARY KEY (settlement_id),
	order_id BIGINT NOT NULL, CONSTRAINT fk_settlement_order FOREIGN KEY (order_id) REFERENCES "order" (order_id),
	seller_id BIGINT NOT NULL, CONSTRAINT fk_settlement_seller FOREIGN KEY (seller_id) REFERENCES member (member_id),
	gross_amount BIGINT NOT NULL,
	commision_fee BIGINT NOT NULL,
	net_amount BIGINT NOT NULL,
	bank_name VARCHAR(100) NOT NULL,
	account_number VARCHAR(500) NOT NULL,
	payout_at TIMESTAMP NOT NULL
);


CREATE TABLE chatting (
    chatting_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_chatting PRIMARY KEY (chatting_id),
    order_id BIGINT NOT NULL, CONSTRAINT fk_chatting_order FOREIGN KEY (order_id) REFERENCES "order" (order_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_chatting_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    content TEXT NOT NULL,
    time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE question (
    question_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_question PRIMARY KEY (question_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_question_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    title VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    answer TEXT,
    answered_at TIMESTAMP
);

CREATE TYPE REPORT_TYPE AS ENUM('AUCTION', 'ORDER', 'CHATTING');
CREATE TYPE REPORT_STATUS AS ENUM('PENDING', 'ACCEPTED', 'REFUNDED');
CREATE TABLE report (
    report_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_report PRIMARY KEY (report_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_report_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    content TEXT NOT NULL,
    type REPORT_TYPE NOT NULL,
    auction_id BIGINT, CONSTRAINT fk_report_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    order_id BIGINT, CONSTRAINT fk_report_order FOREIGN KEY (order_id) REFERENCES "order" (order_id),
    status REPORT_STATUS NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP
);

CREATE TYPE MEMBER_EVENT_TYPE AS ENUM('VIEW', 'CLICK', 'WATCH', 'UNWATCH', 'BID', 'SHARE', 'SEARCH', 'PURCHASE', 'IMPRESSION', 'SCROLL_PASS');
CREATE TABLE member_event (
    member_event_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_member_event PRIMARY KEY (member_event_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_member_event_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    event_type MEMBER_EVENT_TYPE NOT NULL,
    metadata JSONB,
    occured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    auction_id BIGINT NULL, CONSTRAINT fk_member_event_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    product_id BIGINT NULL, CONSTRAINT fk_member_event_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    category_id BIGINT NULL, CONSTRAINT fk_member_event_category FOREIGN KEY (category_id) REFERENCES category (category_id)
);

CREATE TABLE fraud_label (
    fraud_label_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_fraud_label PRIMARY KEY (fraud_label_id),
    auction_id BIGINT NOT NULL, CONSTRAINT fk_fraud_label_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_fraud_label_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT uq_fraud_label_auction_member UNIQUE (auction_id, member_id),
    label SMALLINT NOT NULL,
    label_source VARCHAR(30) NOT NULL,
    reason TEXT,
    confirmed_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fraud_detection (
    detection_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_fraud_detection PRIMARY KEY (detection_id),
    auction_id BIGINT NOT NULL, CONSTRAINT fk_fraud_detection_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_fraud_detection_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    bid_id BIGINT NOT NULL, CONSTRAINT fk_fraud_detection_bid FOREIGN KEY (bid_id) REFERENCES bid (bid_id),
    bidder_tendency DOUBLE PRECISION NOT NULL,
    bidding_ratio DOUBLE PRECISION NOT NULL,
    last_bidding DOUBLE PRECISION NOT NULL,
    auction_bids DOUBLE PRECISION NOT NULL,
    starting_price_average DOUBLE PRECISION NOT NULL,
    early_bidding DOUBLE PRECISION NOT NULL,
    winning_ratio DOUBLE PRECISION NOT NULL,
    auction_duration DOUBLE PRECISION NOT NULL,
	rule_score DOUBLE PRECISION NOT NULL,
	ml_score DOUBLE PRECISION NOT NULL,
    risk_score DOUBLE PRECISION NOT NULL,
    predicted_label SMALLINT NOT NULL,
    decision_threshold DOUBLE PRECISION NOT NULL,
    model_version VARCHAR(50) NOT NULL,
    feature_version VARCHAR(50) NOT NULL,
    detected_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription (
    subscription_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_subscription PRIMARY KEY (subscription_id),
    subscriber_id BIGINT NOT NULL, CONSTRAINT fk_subscription_subscriber FOREIGN KEY (subscriber_id) REFERENCES member (member_id),
    broadcaster_id BIGINT NOT NULL, CONSTRAINT fk_subscription_broadcaster FOREIGN KEY (broadcaster_id) REFERENCES member (member_id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_subscription UNIQUE (subscriber_id, broadcaster_id), -- 중복 구독 방지
    CONSTRAINT chk_subscription_not_self CHECK (subscriber_id != broadcaster_id) -- 자기 자신 구독 불가
);

CREATE TYPE NOTIFICATION_TYPE AS ENUM(
    'LIVE_STARTED',       -- 구독한 회원이 라이브를 켰을 때
    'OUTBID',             -- 내 입찰가가 갱신(상위 입찰 발생)되었을 때
    'BOOKMARK_STARTED',   -- 찜한 경매가 시작되었을 때
    'AUCTION_WON',        -- 경매에서 최종 낙찰되었을 때
    'SYSTEM'              -- 기타 시스템 알림
);
CREATE TABLE notification (
    notification_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_notification PRIMARY KEY (notification_id),
	auction_id BIGINT, CONSTRAINT fk_notification_auction FOREIGN KEY (auction_id) REFERENCES auction (auction_id),
    member_id BIGINT, CONSTRAINT fk_notification_member FOREIGN KEY (member_id) REFERENCES member (member_id),
	live_broadcast_id BIGINT, CONSTRAINT fk_notification_live_broadcast FOREIGN KEY (live_broadcast_id) REFERENCES live_broadcast (live_broadcast_id),
	product_id BIGINT, CONSTRAINT fk_notification_product FOREIGN KEY (product_id) REFERENCES product (product_id),
	bid_id BIGINT, CONSTRAINT fk_notification_bid FOREIGN KEY (bid_id) REFERENCES bid (bid_id),
    type NOTIFICATION_TYPE NOT NULL,
	title VARCHAR(30) NOT NULL,
	content VARCHAR(500) NOT NULL,
	is_read BOOLEAN NOT NULL DEFAULT FALSE,
	created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);