-- 회원 신고가 CHATTING으로 저장되던 문제를 고치려면 DB enum에도 MEMBER 값이 있어야 한다.
ALTER TYPE REPORT_TYPE ADD VALUE IF NOT EXISTS 'MEMBER';
