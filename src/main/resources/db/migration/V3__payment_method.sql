-- 회원 결제 수단 (Toss 빌링키). 회원당 1개 — 교체는 삭제 후 재등록. type은 V2 PAYMENT_TYPE 재사용
CREATE TABLE payment_method (
    payment_method_id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY, CONSTRAINT pk_payment_method PRIMARY KEY (payment_method_id),
    member_id BIGINT NOT NULL, CONSTRAINT fk_payment_method_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    type PAYMENT_TYPE NOT NULL DEFAULT 'CARD',
    billing_key VARCHAR(200) NOT NULL,
    customer_key VARCHAR(100) NOT NULL,
    card_company VARCHAR(50),
    card_number VARCHAR(30),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_method_member UNIQUE (member_id)
);

-- Toss 결제 식별키. 취소(58)·조회에 필요. 기존 행은 NULL 허용
ALTER TABLE payment ADD COLUMN payment_key VARCHAR(200);
