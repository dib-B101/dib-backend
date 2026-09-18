-- 테스트 계정 비밀번호. V900 은 password 에 'hashed_pw_1' 같은 더미 문자열을 넣어서 로그인이 불가능하다.
-- 여기만 실제 BCrypt 해시로 덮어 로그인 가능하게 한다.
--
-- 이 파일은 db/seed 에 있다. seed 는 local 프로필의 flyway locations 에만 들어 있어서
-- 운영에는 적용되지 않는다 (application-prod.yaml 은 classpath:db/migration 만 읽는다).
--
-- 공통 비밀번호: Test1234!
UPDATE member
SET password = '$2b$10$fCVEIqRzgkoCbtvrpgQLKe37rYCuvGsCv8rnX5QNpX9BVZz4cLURC',
    status = 'ACTIVE',
    suspended_at = NULL,
    deleted_at = NULL
WHERE email LIKE 'user%@example.com'
   OR email = 'admin@example.com';
