ALTER TABLE notification
    ADD COLUMN order_id BIGINT,
    ADD CONSTRAINT fk_notification_order
        FOREIGN KEY (order_id) REFERENCES "order" (order_id);

CREATE INDEX idx_notification_order_id ON notification (order_id);
