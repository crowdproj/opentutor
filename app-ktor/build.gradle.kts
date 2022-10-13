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
    val bootstrapVersion: String by project
    val jqueryVersion: String by project
    val keycloakJsVersion: String by project
    val logbackKafkaVersion: String by project
    val janinoVersion: String by project

    implementation(project(":openapi"))
    implementation(project(":mappers"))
    implementation(project(":common"))
    implementation(project(":tts-client"))
    implementation(project(":services"))
    implementation(project(":stubs"))
    implementation(project(":core"))
    implementation(project(":db-common"))
    implementation(project(":db-pg"))
    implementation(project(":db-mem"))

    implementation(project(":logs-openapi"))
    implementation(project(":logs-mappers"))
    implementation(project(":logs-lib"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
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

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")

    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-thymeleaf:$ktorVersion")

    implementation("io.ktor:ktor-server-webjars:$ktorVersion")
    implementation("org.webjars:bootstrap:$bootstrapVersion")
    implementation("org.webjars:jquery:$jqueryVersion")
    implementation("org.webjars.npm:keycloak-js:$keycloakJsVersion")

    implementation("com.github.danielwegener:logback-kafka-appender:$logbackKafkaVersion")
    implementation("org.codehaus.janino:janino:$janinoVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.gitlab.sszuev.flashcards.MainKt")
}

tasks.create("createTagFile") {
    val rootDir = project(":app-ktor").projectDir
    val projectTagFile = Paths.get("$rootDir/project-tag.env")
    val tag = project.version.toString().toLowerCase()
    val projectTagFileContent = "PROJECT_TAG=$tag"
    println(projectTagFileContent)
    Files.writeString(projectTagFile, projectTagFileContent)
}

docker {
    val imageName: String
    val tag = project.version.toString().toLowerCase()
    val javaArgs = mutableListOf("-Xms256m", "-Xmx512m")
    if (System.getProperty("demo") == null) {
        imageName = "sszuev/open-tutor"
    } else {
        println("Build demo image")
        imageName = "sszuev/open-tutor-demo"
        // for demo, we path special parameters
        javaArgs.add("-DKEYCLOAK_DEBUG_AUTH=c9a414f5-3f75-4494-b664-f4c8b33ff4e6")
        javaArgs.add("-DRUN_MODE=test")
    }
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set("adoptopenjdk/openjdk11:alpine-jre")
        maintainer.set("https://github.com/sszuev (sss.zuev@gmail.com)")
        ports.set(listOf(8080))
        images.set(
            listOf(
                "$imageName:$tag",
                "$imageName:latest"
            )
        )
        jvmArgs.set(javaArgs)
    }
}