-- 배포 환경 초기화용 최소 시드.
-- application-prod.yaml 에서만 읽으며 운영 데이터 초기화 후 Flyway가 한 번 적용한다.
-- 이후 배포 초기값은 이 파일에서 관리한다.

-- 관리자 로그인: admin@example.com / Test1234!
INSERT INTO member (
    email,
    password,
    nickname,
    name,
    gender,
    birth_date,
    phone_number,
    status,
    role,
    score
) VALUES (
    'admin@example.com',
    '$2b$10$fCVEIqRzgkoCbtvrpgQLKe37rYCuvGsCv8rnX5QNpX9BVZz4cLURC',
    '관리자',
    '박관리',
    'MALE',
    '1985-11-20',
    '010-9999-9999',
    'ACTIVE',
    'ADMIN',
    5.0
);

INSERT INTO category (name) VALUES
('디지털'),
('생활가전'),
('가구/인테리어'),
('생활/주방'),
('유아동'),
('유아도서'),
('여성의류'),
('여성잡화'),
('남성패션'),
('남성잡화'),
('뷰티/미용'),
('스포츠/레저'),
('취미/게임/음반'),
('도서'),
('티켓/교환권'),
('e쿠폰'),
('가공식품'),
('건강기능식품'),
('반려동물용품'),
('식물'),
('기타');
