# Backend Repository Guide

## Start Here

- 백엔드 개발 원칙: `docs/BACKEND_HARNESS.md`
- Git Flow 및 커밋 정책: `docs/GIT_FLOW.md`
- Pull Request 작성 규칙: `docs/PULL_REQUESTS.md`

## Working Rules

- 코드 또는 구조를 변경하기 전에 `docs/BACKEND_HARNESS.md`에서 적용되는 개발 원칙을 확인한다.
- 브랜치 생성, 커밋, 병합과 삭제는 `docs/GIT_FLOW.md`를 따른다.
- PR 제목과 본문을 작성하거나 제안하기 전에 `docs/PULL_REQUESTS.md`를 읽고 실제 diff와 최신 검증 결과를 반영한다.
- 비밀값과 실제 환경 변수 값은 커밋하지 않는다.
- 사용자 승인 없이 배포, 권한 변경 또는 데이터 삭제를 수행하지 않는다.

## Verification

- 변경 범위에 맞는 테스트를 실행하고 결과를 확인한다.
- PR을 작성하기 전에 대상 브랜치와 실제 diff를 확인한다.
