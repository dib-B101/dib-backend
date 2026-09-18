-- 시작가·경매시간을 정하는 시점이 상품 등록에서 경매 시작/라이브 편성으로 옮겨졌다. NULL = "아직 정하지 않음".
ALTER TABLE auction ALTER COLUMN start_price DROP NOT NULL;
ALTER TABLE auction ALTER COLUMN current_price DROP NOT NULL;
ALTER TABLE auction ALTER COLUMN auction_time DROP NOT NULL;
