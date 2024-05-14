plugins {
    kotlin("jvm")
    id("application")
    id("com.bmuschko.docker-java-application")
}

group = rootProject.group
version = rootProject.version

application {
    mainClass.set("com.gitlab.sszuev.flashcards.cards.CardsServerMainKt")
}

dependencies {
    val kotlinCoroutinesVersion: String by project
    val natsVersion: String by project
    val logbackVersion: String by project
    val junitVersion: String by project
    val slf4jVersion: String by project
    val testContainersVersion: String by project
    val mockkVersion: String by project
    val typesafeConfigVersion: String by project

    implementation(project(":db-api"))
    implementation(project(":db-pg"))
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":utilities"))

    implementation("io.nats:jnats:$natsVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.dockerCreateDockerfile {
    arg("CARDS_SERVER_NATS_HOST")
    arg("DB_PG_URL")
    arg("DB_PG_USER")
    arg("DB_PG_PWD")
    environmentVariable("CARDS_SERVER_NATS_HOST", "\${CARDS_SERVER_NATS_HOST}")
    environmentVariable("DB_PG_URL", "\${DB_PG_URL}")
    environmentVariable("DB_PG_USER", "\${DB_PG_USER}")
    environmentVariable("DB_PG_PWD", "\${DB_PG_PWD}")
}

docker {
    val imageName = "sszuev/open-tutor-cards-server"
    val tag = project.version.toString().lowercase()
    val javaArgs = listOf("-Xms256m", "-Xmx512m", "-DAPP_LOG_LEVEL=debug")
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set("openjdk:23-jdk-slim")
        maintainer.set("https://github.com/sszuev (sss.zuev@gmail.com)")
        images.set(listOf("$imageName:$tag"))
        jvmArgs.set(javaArgs)
    }
}