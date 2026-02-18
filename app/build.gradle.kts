plugins {
    java
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

val springAiVersion = "2.0.0-M1"

group = "org.nembx"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
    }
}

dependencies {
    // Web 模块
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Lombok & 工具
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql")
    annotationProcessor("org.projectlombok:lombok")
//    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    // 测试模块
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Spring AI 2.0 - OpenAI兼容模式
    implementation("org.springframework.ai:spring-ai-starter-model-openai:${springAiVersion}")
    // Spring AI 2.0 - PostgreSQL Vector Store (pgvector)
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector:${springAiVersion}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}