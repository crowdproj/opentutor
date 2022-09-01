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
    val logbackVersion: String by project
    val slf4jVersion: String by project
    val mockkVersion: String by project
    val bootstrapVersion: String by project
    val jqueryVersion: String by project
    val keycloakJsAdapterVersion: String by project

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
    implementation("org.keycloak:keycloak-js-adapter:$keycloakJsAdapterVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

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

docker {
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set("adoptopenjdk/openjdk11:alpine-jre")
        maintainer.set("ssz")
        ports.set(listOf(8080))
        val imageName = project.name
        images.set(
            listOf(
                "$imageName:${project.version}",
                "$imageName:latest"
            )
        )
        jvmArgs.set(listOf("-Xms256m", "-Xmx512m"))
    }
}