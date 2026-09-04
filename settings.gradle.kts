plugins {
    // JDK 자동 프로비저닝 — 로컬에 Java 25가 없으면 Gradle이 직접 내려받는다
    // 1.0.0 = Gradle 9 대응. 0.10.0은 Gradle 9에서 제거된 JvmVendorSpec.IBM_SEMERU를
    // 참조해 settings 평가 단계에서 빌드가 실패한다
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "dib"
