# ── 빌드 스테이지: 레포에 고정된 gradle wrapper 사용 ──
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
# Windows 에서 체크아웃하면 실행 권한이 날아가 ./gradlew 가 permission denied 로 죽는다
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true   # 의존성 레이어 캐시
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# ── 실행 스테이지 ──
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
# 프로필을 명령줄에 박으면 환경변수로 못 바꾼다(명령줄 인자가 우선순위가 더 높다).
# 기본은 prod 로 두고, 로컬 compose 는 SPRING_PROFILES_ACTIVE 로 덮어쓴다
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java","-jar","app.jar"]
