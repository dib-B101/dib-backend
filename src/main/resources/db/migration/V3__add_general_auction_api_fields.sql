CREATE TYPE AUCTION_TYPE AS ENUM ('GENERAL', 'LIVE');

ALTER TABLE auction
    ADD COLUMN auction_type AUCTION_TYPE NOT NULL DEFAULT 'GENERAL',
    ADD COLUMN bid_increment BIGINT NOT NULL DEFAULT 1000,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_auction_general_list
    ON auction (auction_type, status, category_id, created_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_auction_active_end
    ON auction (ended_at ASC)
    WHERE deleted_at IS NULL AND status = 'ACTIVE';
