-- 로컬 개발용 더미데이터 (dib-orchestration/docs/더미데이터.txt 기준)
-- application-local.yaml 의 flyway.locations 에만 포함 → 배포 환경에는 절대 들어가지 않는다


-- 1. 회원 (Member)
INSERT INTO member (email, password, nickname, name, gender, birth_date, phone_number, status, role, score, bank_name, account_holder, account_number) VALUES
('user1@example.com', 'hashed_pw_1', '경매왕김철수', '김철수', 'MALE', '1990-01-01', '010-1111-2222', 'ACTIVE', 'USER', 85.5, '국민은행', '김철수', '123-456-7890'),
('user2@example.com', 'hashed_pw_2', '득템요정', '이영희', 'FEMALE', '1995-05-15', '010-3333-4444', 'ACTIVE', 'USER', 50.0, '신한은행', '이영희', '987-654-3210'),
('admin@example.com', 'hashed_pw_3', '관리자', '박관리', 'MALE', '1985-11-20', '010-9999-9999', 'ACTIVE', 'ADMIN', 100.0, NULL, NULL, NULL);

-- 2. 카테고리 (Category)
INSERT INTO category (name) VALUES
('디지털'),('생활가전'),('가구/인테리어'),('생활/주방'),('유아동'),
('유아도서'),('여성의류'),('여성잡화'),('남성패션'),('남성잡화'),
('뷰티/미용'),('스포츠/레저'),('취미/게임/음반'),('도서'),('티켓/교환권'),
('e쿠폰'),('가공식품'),('건강기능식품'),('반려동물용품'),('식물'),('기타');

-- 3. 상품 (Product)
INSERT INTO product (member_id, category_id, title, description, condition, model_name, release_year, market_price, thumbnail_url, status, embedding) VALUES
(1, 1, '아이폰 13 프로 256GB', '깨끗하게 사용한 아이폰 13 프로입니다. 기스 없습니다.', 'GOOD', 'A2638', 2021, 700000, 'https://example.com/img/iphone13.jpg', 'REGISTERED', NULL),
(2, 2, '나이키 빈티지 바람막이', '실착 3회 미만 상태 A급입니다.', 'NORMAL', 'Nike Windrunner', 2022, 50000, 'https://example.com/img/nike.jpg', 'REGISTERED', NULL),
(1, 3, '해리포터 원서 전권 세트', '소장용으로 가지고 있던 책들입니다.', 'GOOD', NULL, 2010, 80000, 'https://example.com/img/harrypotter.jpg', 'SOLD', NULL);

-- 4. 상품 이미지 (Product Image)
INSERT INTO product_image (product_id, image_url, sequence) VALUES
(1, 'https://example.com/img/iphone13_front.jpg', 1),
(1, 'https://example.com/img/iphone13_back.jpg', 2),
(2, 'https://example.com/img/nike_front.jpg', 3);

-- 5. 주소 (Address)
INSERT INTO address (member_id, number, address, name, api_address_id) VALUES
(1, '04524', '서울특별시 중구 세종대로 110', '우리집', 'API-ADDR-001'),
(2, '46241', '부산광역시 금정구 부산대학로 63', '기숙사', 'API-ADDR-002'),
(2, '06236', '서울특별시 강남구 테헤란로 152', '회사', 'API-ADDR-003');

-- 6. 경매 (Auction)
INSERT INTO auction (
    product_id,
    start_price,
    current_price,
    auction_time,
    started_at,
    ended_at,
    status,
    bid_count,
    bidder_count,
    view_count,
    bookmark_count,
    top_bid_id,
    extension_count,
    created_at,
    updated_at,
    deleted_at,
    live_broadcast_id
) VALUES (
    1,                      -- product_id
    50000,                  -- start_price
    75000,                  -- current_price
    3600,					-- auction_time (1시간)
    '2026-09-07 13:00:00',  -- started_at
    NULL,                   -- ended_at
    'ACTIVE',               -- status
    5,                      -- bid_count
    3,                      -- bidder_count
    127,                    -- view_count
    18,                     -- bookmark_count
    NULL,                   -- top_bid_id
    2,                      -- extension_count
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    NULL,
    NULL                    -- live_broadcast_id
);

-- 7. 입찰 (Bid)
INSERT INTO bid (auction_id, member_id, amount) VALUES
(1, 2, 510000),
(1, 3, 520000),
(1, 2, 600000);

-- 7-1. 종료된 경매 + 낙찰 입찰 (주문 생성 create(auctionId) 테스트용: 경매 2, 판매자 2, 낙찰자 1)
INSERT INTO auction (product_id, start_price, current_price, auction_time, started_at, ended_at, status, bid_count, bidder_count, top_bid_id) VALUES
(2, 30000, 45000, 3600, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '1 hour', 'ENDED', 1, 1, NULL);
INSERT INTO bid (auction_id, member_id, amount) VALUES
(2, 1, 45000);
UPDATE auction SET top_bid_id = (SELECT bid_id FROM bid WHERE auction_id = 2 AND amount = 45000) WHERE auction_id = 2;

-- 8. 찜 (Bookmark)
INSERT INTO bookmark (member_id, product_id) VALUES
(2, 1), (3, 1), (1, 1);

-- 9. 입찰 보증금 (Bid Deposit)
INSERT INTO bid_deposit (member_id, auction_id, amount, status) VALUES
(2, 1, 50000, 'PAID'),
(3, 1, 50000, 'PAID'),
(2, 1, 4000, 'REFUNDED');

-- 10. 주문/거래 (Order)
INSERT INTO "order" (auction_id, seller_id, buyer_id, final_price, status, payment_due, address, tracking_number, chatting_session_id) VALUES
(1, 1, 2, 60000, 'PAID', NOW() + INTERVAL '24 hours', '{"zip": "46241", "addr": "부산광역시 금정구"}', 'CJ-123456789', 'SESSION-001'),
(1, 1, 2, 55000, 'DELIEVERED', NOW() - INTERVAL '3 days', '{"zip": "46241", "addr": "부산광역시 금정구"}', 'CJ-987654321', 'SESSION-002'),
(1, 1, 2, 70000, 'PENDING', NOW() + INTERVAL '12 hours', NULL, NULL, 'SESSION-003');

-- 11. 결제 (Payment)
INSERT INTO payment (order_id, buyer_id, amount, type, refund_key, receipt_url) VALUES
(1, 2, 60000, 'CARD', 'TOSS-KEY-001', 'https://toss.im/receipt/001');

-- 12. 정산 (Settlement) - PK가 문자열임에 주의
INSERT INTO settlement (order_id, seller_id, gross_amount, commision_fee, net_amount, bank_name, account_number, payout_at) VALUES
(1, 1, 60000, 3000, 57000, '국민은행', '123-456-7890', NOW() + INTERVAL '1 day');

-- 13. 채팅 (Chatting)
INSERT INTO chatting (order_id, member_id, content) VALUES
(1, 2, '안녕하세요, 배송 언제쯤 가능할까요?'),
(1, 1, '오늘 오후에 우체국 택배로 접수하겠습니다!'),
(1, 2, '네 감사합니다~');

-- 14. 문의 (Question)
INSERT INTO question (member_id, title, content, answer, answered_at) VALUES
(1, '정산이 아직 안 들어왔어요', '어제 구매 확정이 났는데 정산은 언제 되나요?', '정산은 구매 확정 후 영업일 기준 1~2일 소요됩니다.', NOW()),
(2, '닉네임 변경 제한', '닉네임은 한 달에 몇 번 바꿀 수 있나요?', NULL, NULL),
(1, '불량 사용자 신고 방법', '허위 매물을 올리는 사용자가 있습니다.', '신고 버튼을 이용해주시면 감사하겠습니다.', NOW());

-- 15. 신고 (Report)
INSERT INTO report (member_id, content, type, auction_id, order_id, report_target_id, status) VALUES
(2, '이미지 도용이 의심됩니다.', 'AUCTION', 1, NULL, NULL, 'PENDING'),
(1, '거래 중 욕설을 하네요.', 'CHATTING', NULL, 1, NULL, 'ACCEPTED'),
(3, '허위 송장을 입력했습니다.', 'ORDER', NULL, 1, NULL, 'REFUNDED');

-- 16. 사용자 이벤트 (Member Event)
INSERT INTO member_event (member_id, event_type, metadata, auction_id, product_id, category_id) VALUES
(1, 'BID', '{"device": "MOBILE", "duration": 30}', 1, 1, 1),
(2, 'BID', '{"device": "PC", "bid_amount": 510000}', 1, 1, 1),
(3, 'BID', '{"device": "APP"}', 1, 2, 2);

-- 17. 허위 탐지 라벨 (Fraud Label)
INSERT INTO fraud_label (auction_id, member_id, label, label_source, reason) VALUES
(1, 2, 0, 'ADMIN_REVIEW', '정상적인 입찰 패턴으로 확인됨'),
(1, 1, 1, 'USER_REPORT', '본인 다중 계정 동원 의심'),
(1, 3, 0, 'AI_MODEL', '신뢰도 높은 일반 사용자 거래');

-- 18. 허위 탐지 모델 결과 (Fraud Detection)
INSERT INTO fraud_detection (auction_id, member_id, bid_id, bidder_tendency, bidding_ratio, last_bidding, auction_bids, starting_price_average, early_bidding, winning_ratio, auction_duration, rule_score, ml_score, risk_score, predicted_label, decision_threshold, model_version, feature_version) VALUES
(1, 2, 1, 0.1, 0.5, 0.0, 0.2, 0.8, 1.0, 0.9, 7.0, 0.1, 0.2, 0.15, 0, 0.7, 'v1.0.2', 'f_v2'),
(1, 3, 2, 0.8, 0.9, 1.0, 0.9, 0.2, 0.1, 0.1, 7.0, 0.8, 0.9, 0.88, 1, 0.7, 'v1.0.2', 'f_v2'),
(1, 2, 3, 0.2, 0.3, 0.5, 0.4, 0.9, 0.8, 0.7, 5.0, 0.02, 0.08, 0.05, 0, 0.7, 'v1.0.2', 'f_v2');

-- 19. 라이브 방송 (Live Broadcast)
-- 회원 1(김철수)은 아이폰 경매를 현재 LIVE로 진행 중, 해리포터는 과거에 종료(ENDED)됨
-- 회원 2(이영희)는 나이키 바람막이 경매를 곧 진행할 예정(SCHEDULED)
INSERT INTO live_broadcast (member_id, title, description, status, stream_url, scheduled_at, started_at, ended_at, view_count) VALUES
(1, '아이폰 13 프로 S급 라이브 경매!', '기스 하나 없는 S급 아이폰 실물 라이브로 확인하세요.', 'LIVE', 'https://stream.example.com/live/user1', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '10 minutes', NULL, 150),
(2, '나이키 빈티지 바람막이 득템 찬스', '실착 3회 미만! 상태 아주 좋습니다. 곧 시작합니다.', 'SCHEDULED', NULL, NOW() + INTERVAL '2 hours', NULL, NULL, 0),
(1, '해리포터 원서 전권 경매', '소장용 해리포터 원서 세트 방송입니다.', 'ENDED', 'https://stream.example.com/vod/user1_123', NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days' + INTERVAL '10 minutes', NOW() - INTERVAL '3 days' + INTERVAL '2 hours', 450);

-- 20. 라이브 채팅
INSERT INTO live_chatting (live_broadcast_id, member_id, content, time) VALUES
(1, 1, '안녕하세요', CURRENT_TIMESTAMP),
(1, 2, '만나서 반갑습니다.', CURRENT_TIMESTAMP),
(1, 3, '저도 반가워요', CURRENT_TIMESTAMP);

-- 21. 구독 (Subscription)
-- 유저 간의 팔로우 관계 설정
INSERT INTO subscription (subscriber_id, broadcaster_id) VALUES
(2, 1), -- 이영희(2)가 김철수(1)를 구독
(3, 1), -- 관리자(3)가 김철수(1)를 구독
(1, 2); -- 김철수(1)가 이영희(2)를 구독

-- 22. 알림 (Notification)
-- 구독, 입찰, 낙찰, 찜 시작에 대한 다양한 알림 케이스
INSERT INTO notification (
    auction_id, member_id, live_broadcast_id, product_id, bid_id, type, title, content, is_read, created_at) VALUES
(1, 1, NULL, 1, 1, 'OUTBID', '입찰 성공', '상품에 75,000원으로 입찰하셨습니다.', FALSE, CURRENT_TIMESTAMP),
(1, 2, NULL, 1, NULL, 'AUCTION_WON', '경매 종료', '참여하신 경매가 종료되었습니다.', FALSE, CURRENT_TIMESTAMP),
(1, 3, NULL, 1, NULL, 'LIVE_STARTED', '경매 시작', '관심 상품의 경매가 시작되었습니다.', TRUE, CURRENT_TIMESTAMP);