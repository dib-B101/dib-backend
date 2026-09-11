# Git Flow 정책

이 문서는 `dib-backend` 저장소의 브랜치, 커밋, Pull Request와 병합 규칙이다.

## 장기 브랜치

| 브랜치 | 역할 |
| --- | --- |
| `main` | 배포 가능한 안정 버전을 유지한다. |
| `develop` | 다음 릴리스를 위한 변경을 통합한다. |

장기 브랜치에서 일반 기능을 직접 개발하지 않는다.

## 작업 브랜치

| 유형 | 형식 | 시작 브랜치 | 병합 대상 | 예시 |
| --- | --- | --- | --- | --- |
| 기능·문서·개선 | `feature/<description>` | `develop` | `develop` | `feature/general-login` |
| 릴리스 | `release/<version>` | `develop` | `main`, `develop` | `release/1.2.0` |
| 긴급 수정 | `hotfix/<description>` | `main` | `main`, `develop` | `hotfix/login-timeout` |

`description`은 짧은 영문 소문자 kebab-case를 사용한다.

## Feature 작업 절차

1. 최신 `develop`에서 `feature/<description>` 브랜치를 만든다.
2. 구현과 필요한 문서 변경을 완료한다.
3. 변경 범위에 맞는 테스트를 실행한다.
4. 실제 diff와 테스트 결과를 확인하고 `develop` 대상 PR을 만든다.
5. 리뷰가 끝나면 squash merge한다.
6. 병합 완료를 확인한 후 작업 브랜치를 로컬과 원격 저장소에서 삭제한다.

## Release 작업 절차

1. 릴리스 준비 시점의 `develop`에서 `release/<version>` 브랜치를 만든다.
2. 안정화, 버전 갱신과 문서 보완만 수행한다.
3. 검증 후 `main`에 `--no-ff` merge한다.
4. 동일한 release 브랜치를 `develop`에도 `--no-ff` merge한다.
5. `main`의 릴리스 커밋에 `v<major>.<minor>.<patch>` 태그를 생성한다.
6. 양쪽 반영과 태그를 확인한 후 release 브랜치를 삭제한다.

## Hotfix 작업 절차

1. 운영 긴급 수정이 필요하면 `main`에서 `hotfix/<description>` 브랜치를 만든다.
2. 수정과 회귀 검증을 완료한다.
3. `main`에 `--no-ff` merge하고 새 버전 태그를 생성한다.
4. 동일한 hotfix 브랜치를 `develop`에도 `--no-ff` merge한다.
5. 양쪽 반영과 태그를 확인한 후 hotfix 브랜치를 삭제한다.

## 버전과 태그

Semantic Versioning을 사용한다.

- 버전: `<major>.<minor>.<patch>`
- Git 태그: `v<major>.<minor>.<patch>`
- 예시: `1.2.3`, `v1.2.3`

호환되지 않는 변경은 major, 하위 호환 기능은 minor, 하위 호환 수정은 patch를 증가시킨다. 게시한 태그를 이동하거나 같은 이름으로 다시 만들지 않는다.

## 커밋 메시지

영문 Conventional Commit 타입과 한글 설명을 사용한다.

```text
<type>(<optional-scope>): <한글 설명>
```

| 타입 | 용도 |
| --- | --- |
| `feat` | 새로운 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 변경 |
| `refactor` | 동작 변경 없는 구조 개선 |
| `test` | 테스트 추가 또는 수정 |
| `perf` | 성능 개선 |
| `build` | 빌드 시스템 또는 의존성 변경 |
| `ci` | CI 설정 변경 |
| `chore` | 그 밖의 유지보수 작업 |
| `revert` | 기존 변경 되돌리기 |

예시:

```text
feat(auth): 일반 로그인 기능 구현
fix(auth): 만료된 인증 토큰 오류 처리 수정
docs: Pull Request 작성 규칙 추가
```

## Pull Request와 병합

- PR 제목과 본문은 [`PULL_REQUESTS.md`](PULL_REQUESTS.md)를 따른다.
- PR을 만들기 전에 대상 브랜치, 커밋 범위와 실제 diff를 확인한다.
- 실행한 테스트 명령과 결과를 PR에 기록한다.
- 사용자 승인 없이 장기 브랜치에 직접 push하거나 PR을 병합하지 않는다.
- 병합이 성공적으로 완료된 것을 확인하기 전에는 작업 브랜치를 삭제하지 않는다.
