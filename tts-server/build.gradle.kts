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
    val rabbitVersion: String by project
    val logbackVersion: String by project
    val junitVersion: String by project
    val slf4jVersion: String by project
    val testContainersVersion: String by project
    val mockkVersion: String by project
    val typesafeConfigVersion: String by project

    implementation(project(":tts-lib"))

    implementation("com.rabbitmq:amqp-client:$rabbitVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("org.testcontainers:rabbitmq:$testContainersVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<com.bmuschko.gradle.docker.tasks.image.Dockerfile>("createTTSServerDockerfile") {
    dependsOn("dockerCreateDockerfile")
    from("sszuev/ubuntu-jammy-openjdk-17-espeak-ng")
    label(mapOf("maintainer" to "https://github.com/sszuev (sss.zuev@gmail.com)"))

    arg("TTS_SERVER_RABBITMQ_HOST")
    arg("TTS_SERVICE_VOICERSS_KEY")
    environmentVariable("TTS_SERVER_RABBITMQ_HOST", "\${TTS_SERVER_RABBITMQ_HOST}")
    environmentVariable("TTS_SERVICE_VOICERSS_KEY", "\${TTS_SERVICE_VOICERSS_KEY}")

    workingDir("/app")
    copyFile("libs", "libs/")
    copyFile("resources", "resources/")
    copyFile("classes", "classes/")

    entryPoint(
        "java", "-Xms256m", "-Xmx512m",
        "-DAPP_LOG_LEVEL=debug",
        "-cp", "/app/resources:/app/classes:/app/libs/*",
        application.mainClass.get(),
    )
}

tasks.register<com.bmuschko.gradle.docker.tasks.image.DockerBuildImage>("buildTTSServerDockerImage") {
    dependsOn("createTTSServerDockerfile", "build")
    val imageName = "sszuev/open-tutor-tts-server"
    val tag = project.version.toString().lowercase()
    images.set(listOf("$imageName:$tag"))
}