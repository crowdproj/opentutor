plugins {
    kotlin("jvm")
    id("application")
    id("com.bmuschko.docker-java-application")
}

group = rootProject.group
version = rootProject.version

application {
    mainClass.set("com.gitlab.sszuev.flashcards.speaker.TTSServerMainKt")
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
    arg("CARDS_SERVICE_VOICERSS_KEY")
    environmentVariable("CARDS_SERVER_NATS_HOST", "\${CARDS_SERVER_NATS_HOST}")
    environmentVariable("CARDS_SERVICE_VOICERSS_KEY", "\${CARDS_SERVICE_VOICERSS_KEY}")
}

docker {
    val imageName = "sszuev/open-tutor-cards-server"
    val tag = project.version.toString().lowercase()
    val javaArgs = listOf("-Xms256m", "-Xmx512m", "-DAPP_LOG_LEVEL=debug")
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set("sszuev/ubuntu-jammy-openjdk-17-espeak-ng")
        maintainer.set("https://github.com/sszuev (sss.zuev@gmail.com)")
        images.set(listOf("$imageName:$tag"))
        jvmArgs.set(javaArgs)
    }
}