# 결제 일시와 서버 시간대

`payment.paid_at`은 시간대 정보가 없는 PostgreSQL `TIMESTAMP` / Java `LocalDateTime`이다. 결제 조회 API와 주문·구매 내역 API도 이 값을 시간대 표시 없이 반환한다. Android의 공통 시각 포맷터는 오프셋이 없는 응답을 화면의 현지 시각으로 표시한다. 따라서 새 결제의 `paid_at`에는 KST 벽시계를 기록한다.

결제 승인 시에는 Toss 응답의 `approvedAt` 오프셋을 해석하고 같은 순간을 `Asia/Seoul`로 변환한다. 승인 응답에 시각이 없으면 `Asia/Seoul`의 현재 시각을 사용한다. JVM 기본 시간대에 의존하지 않는다. 이 규칙은 자동결제, 재시도와 웹훅 복구가 공통으로 호출하는 `Payment.approved`에 적용된다.

서버 시간대 점검 결과, 백엔드 Dockerfile과 Kubernetes Deployment에는 JVM 시간대를 지정하는 설정이 없다. 따라서 `LocalDateTime.now()`를 사용하는 다른 도메인의 시각은 실행 환경의 기본 시간대에 따라 달라질 수 있다. DB의 `CURRENT_TIMESTAMP` 기본값도 DB 세션 시간대의 영향을 받는다. 결제 이외의 기존 시각 데이터와 주문 마감·스케줄러는 별도 계약 및 데이터 점검 없이 일괄 변환하지 않는다. 이번 변경은 새 결제에만 적용하며 기존 결제 행을 수정하지 않는다.
