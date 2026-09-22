-- 발행 완료 행 정리(OutboxPublisher.purge)용 인덱스.
-- 기존 ix_outbox_event_unpublished 는 published_at IS NULL 인 행만 담아서 정리 쿼리에 못 쓴다.
-- 이게 없으면 테이블이 커질수록 하루 한 번 도는 DELETE 가 풀스캔이 된다.
CREATE INDEX ix_outbox_event_published_at ON outbox_event (published_at) WHERE published_at IS NOT NULL;

-- 재시도 한도를 넘겨 멈춘 행을 세는 쿼리용. 보통 0건이라 인덱스가 아주 작다
CREATE INDEX ix_outbox_event_attempts ON outbox_event (attempts) WHERE published_at IS NULL;
