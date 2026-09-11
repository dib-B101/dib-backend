# dib-backend

Spring Boot 기반 DIB 백엔드 애플리케이션입니다.

## 로컬 개발 요구사항

- JDK 25
- Docker Engine 또는 Docker Desktop
- Docker Compose

Java 버전은 다음 명령으로 확인합니다.

```bash
java -version
```

## 환경 변수 준비

저장소 루트에서 예시 파일을 `.env`로 복사합니다.

PowerShell:

```powershell
Copy-Item .env.example .env
```

macOS 또는 Linux:

```bash
cp .env.example .env
```

`.env`에는 로컬 포트와 개발용 외부 서비스 설정만 둡니다. 실제 운영 비밀값은 입력하거나 커밋하지 않습니다.

## 로컬 인프라 실행

PostgreSQL, Redis와 Kafka를 실행합니다.

```bash
docker compose up -d
docker compose ps
```

컨테이너를 종료하면서 데이터를 유지하려면 다음 명령을 사용합니다.

```bash
docker compose down
```

볼륨까지 초기화하면 로컬 데이터가 삭제됩니다. 필요한 데이터가 없는지 확인한 후 실행합니다.

```bash
docker compose down -v
```

로컬 프로필에서는 애플리케이션 시작 시 Flyway가 스키마와 개발용 시드 데이터를 반영합니다.

## 애플리케이션 실행

기본 프로필은 `local`이며 서버 포트는 `8080`입니다.

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS 또는 Linux:

```bash
./gradlew bootRun
```

실행 후 다음 경로에서 상태와 API 문서를 확인할 수 있습니다.

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## 테스트

일부 통합 테스트는 Testcontainers를 사용하므로 Docker가 실행 중이어야 합니다.

Windows:

```powershell
.\gradlew.bat test
```

macOS 또는 Linux:

```bash
./gradlew test
```

테스트 결과는 `build/reports/tests/test/index.html`에서 확인합니다.

## 프로젝트 문서

- 백엔드 개발 원칙: [`docs/BACKEND_HARNESS.md`](docs/BACKEND_HARNESS.md)
- Git Flow 및 커밋 정책: [`docs/GIT_FLOW.md`](docs/GIT_FLOW.md)
- Pull Request 작성 규칙: [`docs/PULL_REQUESTS.md`](docs/PULL_REQUESTS.md)
