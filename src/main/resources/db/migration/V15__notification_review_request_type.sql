-- 구매확정 직후 구매자에게 별점을 요청하는 알림 타입.
-- Postgres 는 새 enum 값을 추가한 트랜잭션 안에서 그 값을 쓰지 못한다(PG12+ 에서 추가 자체는 트랜잭션 가능).
-- 그래서 값 추가만 이 파일에 두고, 이 값을 쓰는 테이블·데이터는 V16 에서 만든다.
ALTER TYPE NOTIFICATION_TYPE ADD VALUE IF NOT EXISTS 'REVIEW_REQUEST';
