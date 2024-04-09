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

docker {
    val imageName = "sszuev/open-tutor-tts-server"
    val tag = project.version.toString().lowercase()
    val javaArgs = mutableListOf("-Xms256m", "-Xmx512m")
    val host = if (System.getProperty("tts-server.rabbitmq.host") == null) {
        "flashcards-rabbitmq" // see tutor-deploy/docker-compose-app.yml
    } else {
        System.getProperty("tts-server.rabbitmq.host")
    }
    javaArgs.add("-Dtts-server.rabbitmq.host=$host")
    javaArgs.add("-DAPP_LOG_LEVEL=debug")
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set("sszuev/ubuntu-jammy-openjdk-17-espeak-ng")
        maintainer.set("https://github.com/sszuev (sss.zuev@gmail.com)")
        images.set(listOf("$imageName:$tag"))
        jvmArgs.set(javaArgs)
    }
}