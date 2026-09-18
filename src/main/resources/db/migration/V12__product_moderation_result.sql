-- AI 상품 검수 결과를 상품에 남긴다. 판매자에게 거절 사유를 보여주고, 관리자 검수 근거로 쓴다.
ALTER TABLE product
    ADD COLUMN moderation_reason TEXT,
    ADD COLUMN moderation_stage VARCHAR(20),
    ADD COLUMN moderation_content_hash VARCHAR(128),
    ADD COLUMN moderated_at TIMESTAMP;

-- 검수 대기 큐는 status='PENDING' 을 오래된 순으로 읽는다.
CREATE INDEX IF NOT EXISTS idx_product_status_created_at ON product (status, created_at);

COMMENT ON COLUMN product.moderation_reason IS 'AI 검수 사유(한국어 문장). 판매자에게 그대로 노출한다';
COMMENT ON COLUMN product.moderation_stage IS '판정 단계 rule | ai | fallback';
COMMENT ON COLUMN product.moderation_content_hash IS '검수 대상 내용의 해시. 같으면 재검수를 생략한다';
COMMENT ON COLUMN product.moderated_at IS '검수 결과를 반영한 시각';
