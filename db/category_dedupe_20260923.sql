-- 배포 DB 카테고리 중복 정리 (2026-09-23)
--
-- 배경: 배포 DB 에는 처음부터 8개 분류(디지털기기 · 생활가전 · 가구·인테리어 · 스포츠·레저 · 패션·잡화 · 뷰티 · 취미·게임 · 예술·창작)가
--       있었고 그 분류로 상품이 등록돼 있다. 2026-09-22 cat21.sql 이 V900 의 21개를 "이름이 같지 않으면" 모두 추가해
--       "디지털 / 디지털기기" 처럼 뜻이 겹치는 분류가 두 개씩 보였다.
-- 방침: 기존 8개는 그대로 둔다(상품이 걸려 있다). 뜻이 겹치는 새 분류는 상품·회원 이벤트를 기존 분류로 옮긴 뒤 지운다.
--       겹치지 않는 11개(생활/주방 · 유아동 · 유아도서 · 도서 · 티켓/교환권 · e쿠폰 · 가공식품 · 건강기능식품 · 반려동물용품 · 식물 · 기타)는 남긴다.
-- 실행 전: SELECT category_id, name FROM category ORDER BY category_id; 로 아래 매핑의 이름이 실제 DB 와 같은지 확인한다.
--          기존 분류 이름이 하나라도 다르면 스크립트가 아무것도 지우지 않고 멈춘다.
-- 실행:    psql "$DATABASE_URL" -f category_dedupe_20260923.sql
\set ON_ERROR_STOP on
BEGIN;

-- 되돌릴 수 있도록 현재 category 스냅샷을 남긴다
CREATE TABLE IF NOT EXISTS category_backup_20260923 AS TABLE category;

-- 새로 들어온 분류(dup_name) → 원래 있던 분류(keep_name)
CREATE TEMP TABLE category_merge (dup_name text, keep_name text) ON COMMIT DROP;
INSERT INTO category_merge VALUES
  ('디지털',        '디지털기기'),
  ('가구/인테리어', '가구·인테리어'),
  ('스포츠/레저',   '스포츠·레저'),
  ('여성의류',      '패션·잡화'),
  ('여성잡화',      '패션·잡화'),
  ('남성패션',      '패션·잡화'),
  ('남성잡화',      '패션·잡화'),
  ('뷰티/미용',     '뷰티'),
  ('취미/게임/음반', '취미·게임');

-- 옮겨 받을 기존 분류가 DB 에 없으면(이름이 다르면) 여기서 멈춘다
DO $$
DECLARE missing text;
BEGIN
  SELECT string_agg(m.keep_name, ', ') INTO missing
  FROM (SELECT DISTINCT keep_name FROM category_merge) m
  WHERE NOT EXISTS (SELECT 1 FROM category c WHERE c.name = m.keep_name);
  IF missing IS NOT NULL THEN
    RAISE EXCEPTION '기존 분류 이름이 DB 와 다릅니다: %. category_merge 의 keep_name 을 고친 뒤 다시 실행하세요', missing;
  END IF;
END $$;

-- 겹치는 분류로 등록된 상품과 회원 이벤트를 기존 분류로 옮긴다 (category 를 참조하는 테이블은 이 둘뿐이다)
UPDATE product p SET category_id = keep.category_id
FROM category_merge m
JOIN category dup ON dup.name = m.dup_name
JOIN category keep ON keep.name = m.keep_name
WHERE p.category_id = dup.category_id;

UPDATE member_event e SET category_id = keep.category_id
FROM category_merge m
JOIN category dup ON dup.name = m.dup_name
JOIN category keep ON keep.name = m.keep_name
WHERE e.category_id = dup.category_id;

DELETE FROM category c USING category_merge m WHERE c.name = m.dup_name;

COMMIT;

SELECT category_id, name FROM category ORDER BY category_id;
