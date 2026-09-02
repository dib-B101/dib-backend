plugins {
    // JDK 자동 프로비저닝 — 로컬에 Java 25가 없으면 Gradle이 직접 내려받는다
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "dib"
