-- 신고 접수 시 거래·정산을 보류한다. ORDER_STATUS에 HOLD를 더하면 기존 상태 분기(백엔드·프론트)를 전부 손봐야 하고
-- 보류 해제 시 원래 상태로 되돌릴 수 없으므로, 상태는 그대로 두고 보류 플래그를 따로 둔다.
ALTER TABLE "order" ADD COLUMN held_at TIMESTAMP;
ALTER TABLE "order" ADD COLUMN hold_report_id BIGINT;
ALTER TABLE "order" ADD CONSTRAINT fk_order_hold_report FOREIGN KEY (hold_report_id) REFERENCES report (report_id);

-- 스케줄러가 매분 "보류 아닌 주문"만 훑는다
CREATE INDEX idx_order_held_at ON "order" (held_at) WHERE held_at IS NOT NULL;

-- 신고 기각. 기존에는 PENDING/ACCEPTED/REFUNDED뿐이어서 "혐의 없음"을 기록할 값이 없었다.
ALTER TYPE REPORT_STATUS ADD VALUE IF NOT EXISTS 'REJECTED';
