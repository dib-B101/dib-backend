-- 거래 후기 = 별점만. 코멘트는 받지 않는다.
-- 주문 하나당 한 번, 구매자가 판매자를 평가한다.
CREATE TABLE review (
    review_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY,
        CONSTRAINT pk_review PRIMARY KEY (review_id),
    order_id BIGINT NOT NULL,
        CONSTRAINT fk_review_order FOREIGN KEY (order_id) REFERENCES "order" (order_id),
        CONSTRAINT uq_review_order UNIQUE (order_id),   -- 한 거래에 한 번만
    reviewer_id BIGINT NOT NULL,
        CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES member (member_id),
    seller_id BIGINT NOT NULL,
        CONSTRAINT fk_review_seller FOREIGN KEY (seller_id) REFERENCES member (member_id),
    rating SMALLINT NOT NULL,
        CONSTRAINT ck_review_rating CHECK (rating BETWEEN 0 AND 5),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 판매자 프로필에서 "이 사람의 평점" 을 뽑는 경로
CREATE INDEX ix_review_seller ON review (seller_id);

-- member.score 는 가입 때 50 이 박히고 갱신하는 코드가 한 줄도 없는 죽은 값이었다.
-- 의미를 "받은 별점 평균(0~5)" 으로 바꾸고, 후기가 없으면 NULL 로 둔다.
-- NULL 이어야 프론트가 "아직 평가 없음" 과 "0점" 을 구분해서 가짜 숫자를 안 그린다.
ALTER TABLE member ALTER COLUMN score DROP DEFAULT;
ALTER TABLE member ALTER COLUMN score DROP NOT NULL;
UPDATE member SET score = NULL;   -- 기존 50 은 아무 근거 없는 값이라 버린다

ALTER TABLE member ADD COLUMN review_count INTEGER NOT NULL DEFAULT 0;

-- 별점 요청 알림이 어느 거래 건인지 가리킬 수 있어야 앱이 그 주문 화면을 열어 준다.
-- 배송 시작·결제 완료 같은 기존 주문 알림들도 resourceType 이 SYSTEM 이라 눌러도 아무 데도 못 갔는데,
-- 이 컬럼이 생기면 같이 해결된다.
ALTER TABLE notification ADD COLUMN order_id BIGINT;
ALTER TABLE notification ADD CONSTRAINT fk_notification_order
    FOREIGN KEY (order_id) REFERENCES "order" (order_id);
