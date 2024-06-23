import java.nio.file.Files
import java.nio.file.Paths

plugins {
    kotlin("jvm")
    id("application")
    id("com.bmuschko.docker-java-application")
}

group = rootProject.group
version = rootProject.version

dependencies {
    val ktorVersion: String by project
    val junitVersion: String by project
    val slf4jVersion: String by project
    val mockkVersion: String by project
    val logbackKafkaVersion: String by project
    val janinoVersion: String by project
    val jacksonVersion: String by project

    implementation(project(":openapi"))
    implementation(project(":mappers"))
    implementation(project(":common"))
    implementation(project(":services"))
    implementation(project(":core"))
    implementation(project(":frontend"))

    implementation(project(":logs-openapi"))
    implementation(project(":logs-mappers"))
    implementation(project(":logs-lib"))

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")

    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-auto-head-response:$ktorVersion")
    implementation("io.ktor:ktor-server-forwarded-header:$ktorVersion")

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-apache-jvm:$ktorVersion")

    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-thymeleaf:$ktorVersion")
    implementation("io.ktor:ktor-server-webjars:$ktorVersion")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.github.danielwegener:logback-kafka-appender:$logbackKafkaVersion")
    implementation("org.codehaus.janino:janino:$janinoVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation(project(":db-mem"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.gitlab.sszuev.flashcards.AppMainKt")
}

tasks.create("createTagFile") {
    val rootDir = project(":app-ktor").projectDir
    val projectTagFile = Paths.get("$rootDir/project-tag.env")
    val tag = project.version.toString().lowercase()
    val projectTagFileContent = "PROJECT_TAG=$tag"
    println(projectTagFileContent)
    Files.writeString(projectTagFile, projectTagFileContent)
}

tasks.dockerCreateDockerfile {
    if (System.getProperty("standalone") != null) {
        copyFile("./resources/data/dictionaries.csv", "/app/userdata/dictionaries.csv")
        copyFile("./resources/data/cards.csv", "/app/userdata/cards.csv")
    } else {
        arg("CLIENT_NATS_HOST")
        arg("KEYCLOAK_AUTHORIZE_ADDRESS")
        arg("KEYCLOAK_ACCESS_TOKEN_ADDRESS")
        arg("DB_PG_URL")
        arg("DB_PG_USER")
        arg("DB_PG_PWD")
        arg("PORT")
        environmentVariable("CLIENT_NATS_HOST", "\${CLIENT_NATS_HOST}")
        environmentVariable("KEYCLOAK_AUTHORIZE_ADDRESS", "\${KEYCLOAK_AUTHORIZE_ADDRESS}")
        environmentVariable("KEYCLOAK_ACCESS_TOKEN_ADDRESS", "\${KEYCLOAK_ACCESS_TOKEN_ADDRESS}")
        environmentVariable("DB_PG_URL", "\${DB_PG_URL}")
        environmentVariable("DB_PG_USER", "\${DB_PG_USER}")
        environmentVariable("DB_PG_PWD", "\${DB_PG_PWD}")
        environmentVariable("PORT", "\${PORT}")
    }
}

docker {
    val imageName: String
    val baseImageName: String
    val tag = project.version.toString().lowercase()
    val javaArgs = mutableListOf("-Xms256m", "-Xmx512m", "-DAPP_LOG_LEVEL=debug")
    if (System.getProperty("standalone") == null) {
        imageName = "sszuev/open-tutor"
        baseImageName = "sszuev/ubuntu-jammy-openjdk-17-espeak-ng"
    } else {
        println("Build standalone image")
        imageName = "sszuev/open-tutor-standalone"
        baseImageName = "openjdk:23-jdk-slim"
        // for standalone app, use special (builtin) user uuid & builtin user data (located in /app/userdata)
        javaArgs.add("-DKEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        javaArgs.add("-DRUN_MODE=test")
        javaArgs.add("-DDATA_DIRECTORY=/app/userdata")
    }
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set(baseImageName)
        maintainer.set("https://github.com/sszuev (sss.zuev@gmail.com)")
        ports.set(listOf(8080))
        images.set(listOf("$imageName:$tag", "$imageName:latest"))
        jvmArgs.set(javaArgs)
    }
}