plugins {
    kotlin("jvm")
    id("application")
    id("com.bmuschko.docker-java-application")
}

group = rootProject.group
version = rootProject.version

application {
    mainClass.set("com.gitlab.sszuev.flashcards.translation.TranslationServerMainKt")
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
    val lettuceVersion: String by project
    val kotlinxSerializationVersion: String by project

    implementation(project(":translation-api"))
    implementation(project(":translation-lib"))
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":app-support-lib"))
    implementation(project(":utilities"))

    implementation("io.nats:jnats:$natsVersion")
    implementation("io.lettuce:lettuce-core:$lettuceVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("com.typesafe:config:$typesafeConfigVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")

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
    arg("TRANSLATION_SERVICE_YANDEX_KEY")
    arg("TRANSLATION_SERVER_NATS_HOST")
    arg("TRANSLATION_SERVER_REDIS_HOST")
    environmentVariable("TRANSLATION_SERVICE_YANDEX_KEY", "\${TRANSLATION_SERVICE_YANDEX_KEY}")
    environmentVariable("TRANSLATION_SERVER_NATS_HOST", "\${TRANSLATION_SERVER_NATS_HOST}")
    environmentVariable("TRANSLATION_SERVER_REDIS_HOST", "\${TRANSLATION_SERVER_REDIS_HOST}")
}

docker {
    val imageName = "sszuev/open-tutor-translation-server"
    val tag = project.version.toString().lowercase()
    val javaArgs = listOf("-Xms256m", "-Xmx512m", "-DAPP_LOG_LEVEL=debug")
    javaApplication {
        mainClassName.set(application.mainClass.get())
        baseImage.set("sszuev/openjdk-23-curl:1.0")
        maintainer.set("https://github.com/sszuev (sss.zuev@gmail.com)")
        images.set(listOf("$imageName:$tag"))
        jvmArgs.set(javaArgs)
    }
}