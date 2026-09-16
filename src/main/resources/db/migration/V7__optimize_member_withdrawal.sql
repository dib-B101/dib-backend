CREATE INDEX idx_member_withdrawal_due
    ON member (deleted_at)
    WHERE status = 'ACTIVE'
      AND deleted_at IS NOT NULL;
