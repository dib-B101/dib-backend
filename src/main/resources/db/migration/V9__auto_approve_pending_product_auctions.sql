-- AI 검수 연동 전 임시 정책: 기존 검수 대기 상품도 승인 처리한다.
UPDATE product
SET status = 'REGISTERED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'PENDING'
  AND deleted_at IS NULL;

-- 예전 상품 등록 로직이 PENDING으로 만든 경매를 시작 가능한 예정 상태로 정규화한다.
UPDATE auction a
SET status = 'SCHEDULED',
    updated_at = CURRENT_TIMESTAMP
FROM product p
WHERE a.product_id = p.product_id
  AND a.status = 'PENDING'
  AND p.status = 'REGISTERED'
  AND a.started_at IS NULL
  AND a.ended_at IS NULL
  AND a.deleted_at IS NULL
  AND p.deleted_at IS NULL;
