-- 배송: 택배사 코드 (CJ, HANJIN, LOGEN, LOTTE, EPOST). 배송 조회 시 외부 API 택배사 코드로 변환한다
ALTER TABLE "order" ADD COLUMN carrier VARCHAR(20);

-- 정산: 거래 확정 시점에 행을 만들고 지급 시각·계좌는 나중에 채운다 (기능 명세 "payout_at 기록 여부로 지급 결과 확인")
ALTER TABLE settlement ALTER COLUMN payout_at DROP NOT NULL;
ALTER TABLE settlement ALTER COLUMN bank_name DROP NOT NULL;
ALTER TABLE settlement ALTER COLUMN account_number DROP NOT NULL;
