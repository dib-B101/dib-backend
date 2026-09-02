# ── 빌드 스테이지: 레포에 고정된 gradle wrapper 사용 ──
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || true   # 의존성 레이어 캐시
COPY src ./src
RUN ./gradlew bootJar --no-daemon

# ── 실행 스테이지 ──
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar","--spring.profiles.active=prod"]
