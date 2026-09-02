-- product 테이블 (Product 엔티티와 1:1 대응 — ddl-auto: validate 이므로 컬럼명/타입을 엔티티와 맞춘다)
CREATE TABLE product (
    product_id      BIGSERIAL PRIMARY KEY,
    member_id       BIGINT,
    category_id     BIGINT,
    title           VARCHAR(255),
    description     TEXT,
    condition       VARCHAR(255),          -- enum ProductCondition (GOOD / NORMAL / BAD)
    model_name      VARCHAR(255),
    release_year    INTEGER,
    market_price    BIGINT,
    thumbnail_url   VARCHAR(255),
    status          VARCHAR(255),          -- enum ProductStatus (DRAFT / ACTIVE / SOLD / HIDDEN)
    embedding       TEXT,
    created_at      TIMESTAMP(6),
    updated_at      TIMESTAMP(6),
    deleted_at      TIMESTAMP(6)
);

CREATE INDEX idx_product_created_at ON product (created_at DESC);
